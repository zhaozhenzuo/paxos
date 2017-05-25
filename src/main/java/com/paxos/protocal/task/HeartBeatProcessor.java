package com.paxos.protocal.task;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.paxos.core.component.PaxosCoreComponent;
import com.paxos.core.constants.CodeInfo;
import com.paxos.core.domain.PaxosMember;
import com.paxos.extrange.DefaultExChangeClient;
import com.paxos.extrange.DefaultExChrangeServer;
import com.paxos.extrange.ExchangeClient;
import com.paxos.extrange.ExchangeServer;
import com.paxos.protocal.handler.UpStreamHandler;
import com.paxos.remoting.api.RemotingCommand;
import com.paxos.util.RequestAndResponseUtil;

@Component
public class HeartBeatProcessor {

	private static final Logger logger = Logger.getLogger(HeartBeatProcessor.class);

	@Autowired
	private PaxosCoreComponent memberService;

	/**
	 * 默认心跳检查时间
	 */
	private static final long DEFAULT_HEARTBEAT_TIME_MILLISECONDS = 1000;

	private ScheduledExecutorService scheduledExecutorService;

	private ExchangeServer heatbeatExchangeServer;

	private Lock lock = new ReentrantLock();

	private Map<String/* 成员ip+port构成唯一标识 */, ExchangeClient/* 与其它结点的socket连接 */> exchangeClientCacheMap;

	private UpStreamHandler upStreamHandler;

	public void stop() {
		scheduledExecutorService.shutdownNow();
	}

	public HeartBeatProcessor() {

	}

	public void start(UpStreamHandler upStreamHandler) {
		if (upStreamHandler == null) {
			throw new IllegalArgumentException("upstreamHandler不能为空");
		}

		this.upStreamHandler = upStreamHandler;
		PaxosMember currentMember = memberService.getCurrentPaxosMember();
		List<PaxosMember> otherMemberList = memberService.getOtherPaxosMemberList();

		/**
		 * 1.如果无其它结点就不需要有心跳了
		 */
		if (CollectionUtils.isEmpty(otherMemberList)) {
			return;
		}
		exchangeClientCacheMap = new HashMap<String, ExchangeClient>();

		/**
		 * 2.开启本地监听心跳server
		 */
		heatbeatExchangeServer = new DefaultExChrangeServer(upStreamHandler);
		int heartbeatPort = this.getHeartbeatPortByPort(currentMember.getPort());
		heatbeatExchangeServer.start(currentMember.getIp(), heartbeatPort);

		/**
		 * 3.开启心跳定时器
		 */
		scheduledExecutorService = Executors.newScheduledThreadPool(1);
		scheduledExecutorService.scheduleWithFixedDelay(new HeartBeatTask(), 1000, DEFAULT_HEARTBEAT_TIME_MILLISECONDS,
				TimeUnit.MILLISECONDS);

	}

	private void reconnect(PaxosMember paxosMember) {
		lock.lock();

		try {

			/**
			 * 1.获取对方心跳ip及端口
			 */
			String ip = paxosMember.getIp();
			// 心跳端口是选举端口+1
			int heatbeatPort = getHeartbeatPortByPort(paxosMember.getPort());
			logger.debug(">begin heartbeat connect to server,ip[" + ip + "],heatbeatPort[" + heatbeatPort + "]");

			/**
			 * 2.如果之前有连接，则需要先关闭
			 */
			String exchangeClientKey = this.getExchangeClientCacheKey(ip, heatbeatPort);
			ExchangeClient exchangeClient = exchangeClientCacheMap.get(exchangeClientKey);
			if (exchangeClient != null && !exchangeClient.isStoped()) {
				exchangeClient.stop();
				exchangeClientCacheMap.remove(exchangeClientKey);
			}

			/**
			 * 3.创建新的exchangeClient并重新连接
			 */
			UpStreamHandler freshUpstreamHandler = new UpStreamHandler(upStreamHandler.getPaxosCoreComponent());
			ExchangeClient exchangeClientFresh = new DefaultExChangeClient(freshUpstreamHandler);
			boolean connectSuccFlag = exchangeClientFresh.connect(ip, heatbeatPort);
			if (!connectSuccFlag) {
				// TODO,test
				logger.debug(">can not connect to server,ip[" + ip + "],heatbeatPort[" + heatbeatPort + "]");
				paxosMember.setIsUp(false);
			} else {
				/**
				 * 连接成功
				 */
				paxosMember.setIsUp(true);
				exchangeClientCacheMap.put(exchangeClientKey, exchangeClientFresh);
				logger.debug(">end heartbeat succ connect to server,ip[" + ip + "],port[" + heatbeatPort + "]");
			}
		} finally {
			lock.unlock();
		}
	}

	public ExchangeClient getExchangeClientReconnectWhenNotExist(PaxosMember paxosMember) {
		String ip = paxosMember.getIp();
		int port = paxosMember.getPort();
		String key = getExchangeClientCacheKey(ip, port);
		ExchangeClient exchangeClient = exchangeClientCacheMap.get(key);
		if (exchangeClient != null) {
			return exchangeClient;
		}

		/**
		 * 没有进行重连然后返回该连接
		 */
		logger.info(">getExchangeClientReconnectWhenNotExist not found exchangeClient and try to reconnect,ip[" + ip + "],port[" + port
				+ "]");
		this.reconnect(paxosMember);

		exchangeClient = exchangeClientCacheMap.get(key);
		if (exchangeClient == null) {
			return null;
		}
		return exchangeClient;
	}

	class HeartBeatTask implements Runnable {

		@Override
		public void run() {

			logger.debug(">begin heartbeat task");

			List<PaxosMember> otherMemberList = memberService.getOtherPaxosMemberList();

			for (PaxosMember paxosMember : otherMemberList) {
				logger.debug(">===============ip[" + paxosMember.getIp() + "],port[" + paxosMember.getPort() + "] status["
						+ paxosMember.getIsUp() + "]");
				if (!paxosMember.getIsUp()) {
					/**
					 * 对方结点状态如果是异常的话，需要重连
					 */
					reconnect(paxosMember);
				} else {
					/**
					 * 如果与其它结点是活跃的情况下，需要发送心跳
					 */
					this.processLiveTime(paxosMember);

				}

			}
			logger.debug(">end heartbeat task");
		}

		/**
		 * 处理心跳保活
		 */
		private void processLiveTime(PaxosMember paxosMember) {

			/**
			 * 1.获取对应exchangeClient的key
			 */
			String ip = paxosMember.getIp();
			int heartbeatPort = getHeartbeatPortByPort(paxosMember.getPort());
			String exchangeClientKey = getExchangeClientCacheKey(ip, heartbeatPort);

			/**
			 * 2.获取对应exchangeClient
			 */
			ExchangeClient exchangeClient = exchangeClientCacheMap.get(exchangeClientKey);
			if (exchangeClient == null || exchangeClient.isStoped()) {
				/**
				 * 如果当前连接已经关闭，则重连
				 */
				reconnect(paxosMember);
				return;
			}

			/**
			 * 3.判断当前连接是否超过
			 */
			long lastLiveTime = exchangeClient.getLastLiveTime();
			long currentTime = new Date().getTime();
			if (currentTime - lastLiveTime > CodeInfo.DEFAULT_CHANNEL_LIVE_TIME_OUT_MILLISECONDS) {
				/**
				 * 如果超过保活最长时间则进行重连
				 */
				reconnect(paxosMember);
				return;
			}

			/**
			 * 4.还未到连接保存超时时间，发送心跳
			 */
			RemotingCommand heartbeatReqCommand = RequestAndResponseUtil.composeHeartbeatRequestCommand();
			try {
				exchangeClient.sendAsyncSync(heartbeatReqCommand);
			} catch (Exception e) {
				// TODO,just test
				logger.debug(">send heartbeat err,remote ip[" + ip + "],remote heartbeatPort[" + heartbeatPort + "]");
			}
		}

	}

	private int getHeartbeatPortByPort(int port) {
		return port;
	}

	private String getExchangeClientCacheKey(String ip, int port) {
		return ip + CodeInfo.IP_AND_PORT_SPLIT + port;
	}

}
