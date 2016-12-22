package com.paxos.extrange;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import com.paxos.protocal.handler.UpStreamHandler;
import com.paxos.remoting.api.RemotingClient;
import com.paxos.remoting.api.RemotingCommand;
import com.paxos.remoting.netty.DefaultRemotingClient;

public class DefaultExChangeClient implements ExchangeClient {

	private RemotingClient remotingClient;

	/**
	 * 上次这条以通信的心跳更新时间
	 */
	private long lastLiveTime;

	private AtomicBoolean stopedFlag = new AtomicBoolean(false);

	private UpStreamHandler upStreamHandler;

	public DefaultExChangeClient(UpStreamHandler upStreamHandler) {
		lastLiveTime = new Date().getTime();
		this.upStreamHandler = upStreamHandler;
	}

	public Object sendSync(RemotingCommand req) throws Exception {
		/**
		 * 1.注册future
		 */
		DefaultFuture defaultFuture = new DefaultFuture(req, this);

		/**
		 * 2.异步发送消息
		 */
		remotingClient.send(req);

		/**
		 * 3.阻塞同步获取结果
		 */
		return defaultFuture.get();
	}

	public boolean connect(String ip, Integer port) {
		remotingClient = new DefaultRemotingClient(upStreamHandler);
		return remotingClient.connect(ip, port);
	}

	@Override
	public void stop() {
		remotingClient.stop();
		stopedFlag.set(true);
	}

	public long getLastLiveTime() {
		return lastLiveTime;
	}

	public void setLastLiveTime(long lastLiveTime) {
		this.lastLiveTime = lastLiveTime;
	}

	@Override
	public boolean isStoped() {
		return stopedFlag.get();
	}

	@Override
	public ResponseFuture sendAsyncSync(RemotingCommand req) throws Exception {
		/**
		 * 1.注册future
		 */
		DefaultFuture defaultFuture = new DefaultFuture(req, this);

		/**
		 * 2.异步发送消息
		 */
		remotingClient.send(req);
		
		return defaultFuture;
	}

}
