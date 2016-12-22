package com.paxos.util;

import com.alibaba.fastjson.JSON;
import com.paxos.core.constants.CodeInfo;
import com.paxos.core.domain.BaseElectionResponse;
import com.paxos.core.domain.ElectionRequest;
import com.paxos.core.domain.ElectionResponse;
import com.paxos.core.domain.ElectionResultRequest;
import com.paxos.core.domain.ElectionResultResponse;
import com.paxos.core.domain.HeartbeatReponse;
import com.paxos.core.domain.SearchInfoResponse;
import com.paxos.remoting.api.RemotingCommand;
import com.paxos.remoting.api.RemotingRequest;
import com.paxos.remoting.api.RemotingResponse;

public class RequestAndResponseUtil {

	public static RemotingCommand composeSearchInfoRequest(long reqId) {
		RemotingCommand remotingCommand = new RemotingCommand(CodeInfo.COMMAND_TYPE_REQ);

		RemotingRequest remotingRequest = new RemotingRequest(reqId);
		remotingRequest.setType(CodeInfo.REQ_TYPE_SEARCH_INFO);
		remotingCommand.setBody(JSON.toJSONString(remotingRequest));

		return remotingCommand;
	}

	public static RemotingCommand composeSearchInfoResponse(long reqId, SearchInfoResponse searchInfoResponse) {
		RemotingCommand remotingCommand = new RemotingCommand(CodeInfo.COMMAND_TYPE_RES);

		RemotingResponse remotingResponse = new RemotingResponse();
		remotingResponse.setReqId(reqId);
		remotingResponse.setData(JSON.toJSONString(searchInfoResponse));

		remotingCommand.setBody(JSON.toJSONString(remotingResponse));
		return remotingCommand;
	}

	/**
	 * 构建选举通信结果,底层会分别根据业务类型及election结果构造<br/>
	 * 目前支持：一阶段结果，二阶段结果，及接受选举结果返回的响应
	 * 
	 * @param reqId
	 * @param bizType
	 * @param electionResponse
	 * @return
	 */
	public static RemotingCommand composeElectionResponseRemotingCommand(long reqId, byte bizType, BaseElectionResponse electionResponse) {
		RemotingCommand remotingCommand = null;
		if (CodeInfo.REQ_TYPE_ELECTION_FIRST_PHASE == bizType) {
			remotingCommand = RequestAndResponseUtil.composeFirstPahseElectionResponseCommand(reqId, electionResponse);
		} else if (CodeInfo.REQ_TYPE_ELECTION_SECOND_PHASE == bizType) {
			remotingCommand = RequestAndResponseUtil.composeSecondPahseElectionResponseCommand(reqId, electionResponse);
		} else if (CodeInfo.REQ_TYPE_ELECTION_RESULT_TO_LEANER == bizType) {
			remotingCommand = composeElectionResultResponseCommand(reqId, electionResponse);
		} else {
			throw new IllegalArgumentException("不支持的选举阶段类型,type[" + bizType + "]");
		}
		return remotingCommand;
	}

	public static RemotingCommand composeFirstPahseElectionResponseCommand(long reqId, BaseElectionResponse electionResponse) {
		RemotingCommand remotingCommand = new RemotingCommand(CodeInfo.COMMAND_TYPE_RES);

		RemotingRequest remotingRequest = new RemotingRequest(reqId);
		remotingRequest.setType(CodeInfo.RES_TYPE_ELECTION_FIRST_PHASE);
		remotingRequest.setData(JSON.toJSONString(electionResponse));
		remotingCommand.setBody(JSON.toJSONString(remotingRequest));
		return remotingCommand;
	}

	public static RemotingCommand composeSecondPahseElectionResponseCommand(long reqId, BaseElectionResponse electionResponse) {
		RemotingCommand remotingCommand = new RemotingCommand(CodeInfo.COMMAND_TYPE_RES);

		RemotingRequest remotingRequest = new RemotingRequest(reqId);
		remotingRequest.setType(CodeInfo.RES_TYPE_ELECTION_SECOND_PHASE);

		// 实际业务一阶段选举响应结果
		remotingRequest.setData(JSON.toJSONString(electionResponse));
		remotingCommand.setBody(JSON.toJSONString(remotingRequest));
		return remotingCommand;
	}

	/**
	 * 构造选举请求
	 * 
	 * @param round
	 *            选举轮数
	 * @param phase
	 *            第几阶段,CodeInfo.FIRST_PHASE_SEQ或CodeInfo.SECOND_PHASE_SEQ
	 * @param num
	 * @param value
	 * @return
	 */
	public static RemotingCommand composeElectionRequest(long round, int phase, long num, Object value) {
		String logStr = "phase[" + phase + "],num[" + num + "],value[" + value + "]";

		if (phase != CodeInfo.FIRST_PHASE_SEQ && phase != CodeInfo.SECOND_PHASE_SEQ) {
			throw new IllegalArgumentException("composeElectionRequest err,phase must be 1 or 2," + logStr);
		}

		RemotingCommand command = new RemotingCommand(CodeInfo.COMMAND_TYPE_REQ);

		RemotingRequest remotingRequest = new RemotingRequest(RemotingRequest.getAndIncreaseReq());
		ElectionRequest electionRequest = new ElectionRequest(phase, num, value);
		electionRequest.setElectionRound(round);
		remotingRequest.setData(JSON.toJSONString(electionRequest));

		if (phase == CodeInfo.FIRST_PHASE_SEQ) {
			remotingRequest.setType(CodeInfo.REQ_TYPE_ELECTION_FIRST_PHASE);
		} else {
			remotingRequest.setType(CodeInfo.REQ_TYPE_ELECTION_SECOND_PHASE);
		}

		command.setBody(JSON.toJSONString(remotingRequest));
		return command;
	}

	public static RemotingCommand composeElectionResultRequest(long electionRound, long realNum, Object realValue) {
		RemotingCommand command = new RemotingCommand(CodeInfo.COMMAND_TYPE_REQ);
		RemotingRequest remotingRequest = new RemotingRequest(RemotingRequest.getAndIncreaseReq());
		remotingRequest.setType(CodeInfo.REQ_TYPE_ELECTION_RESULT_TO_LEANER);

		// 业务发送选举结果对象
		ElectionResultRequest electionResultRequest = new ElectionResultRequest();
		electionResultRequest.setNum(realNum);
		electionResultRequest.setValue(realValue);
		electionResultRequest.setElectionRound(electionRound);
		remotingRequest.setData(JSON.toJSONString(electionResultRequest));

		command.setBody(JSON.toJSONString(remotingRequest));
		return command;
	}

	/**
	 * 构建learner接收到选举结果请求处理后，返回给提议者的结果值
	 * 
	 * @param code
	 * @return
	 */
	public static RemotingCommand composeElectionResultResponseCommand(long reqId, int code) {
		RemotingCommand command = new RemotingCommand(CodeInfo.COMMAND_TYPE_RES);
		RemotingRequest remotingRequest = new RemotingRequest(reqId);
		remotingRequest.setType(CodeInfo.RES_TYPE_ELECTION_RESULT_TO_LEANER);

		// 业务发送选举结果对象
		ElectionResultResponse electionResultResponse = composeElectionResultResponse(code);
		remotingRequest.setData(JSON.toJSONString(electionResultResponse));
		command.setBody(JSON.toJSONString(remotingRequest));
		return command;
	}

	public static RemotingCommand composeElectionResultResponseCommand(long reqId, BaseElectionResponse electionResultResponse) {
		RemotingCommand command = new RemotingCommand(CodeInfo.COMMAND_TYPE_RES);
		RemotingRequest remotingRequest = new RemotingRequest(reqId);
		remotingRequest.setType(CodeInfo.RES_TYPE_ELECTION_RESULT_TO_LEANER);

		// 业务发送选举结果对象
		remotingRequest.setData(JSON.toJSONString(electionResultResponse));
		command.setBody(JSON.toJSONString(remotingRequest));
		return command;
	}

	public static ElectionResultResponse composeElectionResultResponse(int code) {
		// 业务发送选举结果对象
		ElectionResultResponse electionResultResponse = new ElectionResultResponse();
		electionResultResponse.setCode(code);
		return electionResultResponse;
	}

	public static ElectionResponse composeSecondPahseElectionResponse(String responseCode, Long maxAcceptFirstPhaseNum,
			Long maxAcceptSecondPhaseNum, Object maxAcceptSecondPhaseValue) {
		// 实际业务一阶段选举响应结果
		return new ElectionResponse(responseCode, maxAcceptFirstPhaseNum, maxAcceptSecondPhaseNum, maxAcceptSecondPhaseValue);
	}

	public static ElectionResponse composeFirstPahseElectionResponse(String responseCode, Long maxAcceptFirstPhaseNum,
			Long maxAcceptSecondPhaseNum, Object maxAcceptSecondPhaseValue, Long realNum, Object realValue, Long electionFinishRound) {
		// 实际业务一阶段选举响应结果
		ElectionResponse electionResponse = new ElectionResponse(responseCode, maxAcceptFirstPhaseNum, maxAcceptSecondPhaseNum,
				maxAcceptSecondPhaseValue);
		electionResponse.setRealNum(realNum);
		electionResponse.setRealValue(realValue);
		electionResponse.setElectionRound(electionFinishRound);
		return electionResponse;
	}

	public static RemotingCommand composeHeartbeatResponseCommand(long reqId) {
		/**
		 * 心跳请求
		 */
		RemotingCommand responseCommand = new RemotingCommand();
		responseCommand.setVersion(CodeInfo.VERSION);
		responseCommand.setCommandType(CodeInfo.COMMAND_TYPE_RES);

		RemotingResponse remotingResponse = new RemotingResponse();
		remotingResponse.setReqId(reqId);
		remotingResponse.setType(CodeInfo.RES_TYPE_HEART_BEAT);

		/**
		 * 心跳返回对象
		 */
		HeartbeatReponse heartbeatReponse = new HeartbeatReponse();
		heartbeatReponse.setStatus(CodeInfo.HEATBEAT_RES_OK);
		remotingResponse.setData(JSON.toJSONString(heartbeatReponse));

		responseCommand.setBody(JSON.toJSONString(remotingResponse));
		return responseCommand;
	}

	public static RemotingCommand composeHeartbeatRequestCommand() {
		RemotingCommand heartbeatRequestCommand = new RemotingCommand();
		heartbeatRequestCommand.setVersion(CodeInfo.VERSION);
		heartbeatRequestCommand.setCommandType(CodeInfo.COMMAND_TYPE_REQ);

		/**
		 * 心跳对象
		 */
		RemotingRequest remotingRequest = new RemotingRequest(RemotingRequest.getAndIncreaseReq());
		remotingRequest.setType(CodeInfo.REQ_TYPE_HEART_BEAT);
		String data = "1";
		remotingRequest.setData(JSON.toJSONString(data));

		heartbeatRequestCommand.setBody(JSON.toJSONString(remotingRequest));
		return heartbeatRequestCommand;
	}

}
