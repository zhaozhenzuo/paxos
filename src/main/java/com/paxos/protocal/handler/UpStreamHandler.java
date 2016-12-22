package com.paxos.protocal.handler;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.paxos.core.component.PaxosCoreComponent;
import com.paxos.core.constants.CodeInfo;
import com.paxos.core.domain.BaseElectionResponse;
import com.paxos.extrange.DefaultFuture;
import com.paxos.extrange.ExchangeClient;
import com.paxos.remoting.api.RemotingCommand;
import com.paxos.remoting.api.RemotingRequest;
import com.paxos.remoting.api.RemotingResponse;
import com.paxos.util.RequestAndResponseUtil;

/**
 * 结点接收到远程通信指令处理类
 * 
 * @author zhaozhenzuo
 *
 */
public class UpStreamHandler extends SimpleChannelInboundHandler<RemotingCommand> {

	private static final Logger logger = Logger.getLogger(UpStreamHandler.class);

	private PaxosCoreComponent paxosCoreComponent;

	public UpStreamHandler(PaxosCoreComponent paxosCoreComponent) {
		this.paxosCoreComponent = paxosCoreComponent;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RemotingCommand msg) throws Exception {
		logger.debug(">recieve msg,msg[" + JSON.toJSONString(msg) + "]");

		/**
		 * 1.检查msg有效性
		 */
		/**
		 * 2.查询结果
		 */
		String bizReq = msg.getBody();

		/**
		 * 3.处理指令
		 */
		if (msg.getCommandType() == CodeInfo.COMMAND_TYPE_REQ) {
			/**
			 * 3.1.处理远程通信请求指令
			 */
			RemotingRequest remotingRequest = RemotingRequest.parseCommandToRemotingRequest(bizReq);
			this.processCommandReq(ctx, remotingRequest);
		} else if (msg.getCommandType() == CodeInfo.COMMAND_TYPE_RES) {
			/**
			 * 3.2.处理远程通信响应指令
			 */
			RemotingResponse remotingResponse = RemotingRequest.parseCommandToRemotingResponse(bizReq);
			this.processCommandResponse(ctx, remotingResponse);
		} else {
			throw new IllegalArgumentException("不支持的command类型,parma[" + JSON.toJSONString(msg) + "]");
		}

	}

	private void processCommandResponse(ChannelHandlerContext ctx, RemotingResponse remotingResponse) {
		String msgLog = JSON.toJSONString(remotingResponse);
		logger.debug(">begin processCommandResponse,remotingResponse[" + msgLog + "]");

		/**
		 * 1.处理响应
		 */
		if (remotingResponse.getType() == CodeInfo.RES_TYPE_HEART_BEAT && !StringUtils.isEmpty(remotingResponse.getData())) {
			/**
			 * 如果是心跳响应则更新对应exchangeClient的心跳保活时间
			 */
			ExchangeClient exchangeClient = DefaultFuture.getExchangeClientByReqId(remotingResponse.getReqId());
			if (exchangeClient == null) {
				logger.error(">cannot found exchangeClient,remotingResponse[" + msgLog + "]");
			} else {
				exchangeClient.setLastLiveTime(new Date().getTime());
			}
		}

		/**
		 * 2.设置异步结果，并清除defaultFuture中的请求上下文
		 */
		DefaultFuture.recieve(remotingResponse);
		logger.debug(">end processCommandResponse succ,remotingResponse[" + msgLog + "]");
	}

	private void processCommandReq(ChannelHandlerContext ctx, RemotingRequest remotingRequest) {
		String logStr = JSON.toJSONString(remotingRequest);
		logger.debug(">begin processCommandReq,request[" + logStr + "]");

		/**
		 * 1.心跳处理
		 */
		if (CodeInfo.REQ_TYPE_HEART_BEAT == remotingRequest.getType()) {
			RemotingCommand responseCommand = RequestAndResponseUtil.composeHeartbeatResponseCommand(remotingRequest.getReqId());
			logger.debug(">send heartbeat response,reponse[" + logStr + "]");
			ChannelFuture channelFuture = ctx.channel().writeAndFlush(responseCommand);
			logger.debug("send response succFlag[" + channelFuture.isSuccess() + "]");
			return;
		}

		/**
		 * 2.其它为选举业务处理，交给业务组件
		 */
		String requestData = remotingRequest.getData();
		byte bizType = remotingRequest.getType();
		long reqId = remotingRequest.getReqId();
		boolean sendResponseFlag;
		RemotingCommand resCommand;

		BaseElectionResponse electionResponse = paxosCoreComponent.processElectionRequestForAcceptor(bizType, requestData);
		resCommand = RequestAndResponseUtil.composeElectionResultResponseCommand(reqId, electionResponse);
		sendResponseFlag = this.secondRemotingCommand(ctx, resCommand);
		if (!sendResponseFlag) {
			logger.error(">processCommandReq send responseRes err," + logStr);
		}
		logger.debug(">end processCommandReq succ,request[" + logStr + "]");
	}

	private boolean secondRemotingCommand(ChannelHandlerContext ctx, RemotingCommand remotingCommand) {
		String logStr = JSON.toJSONString(remotingCommand);
		try {
			ChannelFuture sendRes = ctx.writeAndFlush(remotingCommand);
			if (!sendRes.isSuccess()) {
				logger.error(">secondRemotingCommand err," + logStr);
				return false;
			}
			return true;
		} catch (Exception e) {
			logger.error(">secondRemotingCommand err,param[" + logStr + "]");
			return false;
		}
	}

	public PaxosCoreComponent getPaxosCoreComponent() {
		return paxosCoreComponent;
	}

	public void setPaxosCoreComponent(PaxosCoreComponent paxosCoreComponent) {
		this.paxosCoreComponent = paxosCoreComponent;
	}
}
