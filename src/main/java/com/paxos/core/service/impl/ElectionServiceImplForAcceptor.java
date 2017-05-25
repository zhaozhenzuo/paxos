package com.paxos.core.service.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.paxos.core.constants.CodeInfo;
import com.paxos.core.domain.ElectionRequest;
import com.paxos.core.domain.ElectionResponse;
import com.paxos.core.domain.ElectionResultRequest;
import com.paxos.core.domain.ElectionResultResponse;
import com.paxos.core.domain.PaxosMember;
import com.paxos.core.service.ElectionServiceForAcceptor;
import com.paxos.core.store.PaxosStoreInf;
import com.paxos.enums.PaxosMemberStatus;
import com.paxos.util.RequestAndResponseUtil;

@Service
public class ElectionServiceImplForAcceptor implements ElectionServiceForAcceptor {

	private static final Logger logger = Logger.getLogger(ElectionServiceImplForAcceptor.class);

	@Autowired
	private PaxosStoreInf paxosStore;

	/**
	 * 接收者接到一阶段请求
	 * 
	 * @param ctx
	 * @param request
	 */
	@Override
	public ElectionResponse processElectionRequestFirstPhase(ElectionRequest electionRequest) {
		String logStr = JSON.toJSONString(electionRequest);
		PaxosMember currentMember = paxosStore.getCurrentPaxosMember();
		Long maxAcceptFirstPhaseNum = currentMember.getElectionInfo().getMaxAcceptFirstPhaseNum();
		Long maxAcceptSecondPhaseNum = currentMember.getElectionInfo().getMaxAcceptSecondPhaseNum();
		Object maxAcceptSecondPhaseValue = currentMember.getElectionInfo().getMaxAcceptSecondPhaseValue();
		Long electionRoundSaved = currentMember.getElectionInfo().getElectionRound();
		Long realNumSaved = currentMember.getElectionInfo().getRealNum();
		Object realValueSaved = currentMember.getElectionInfo().getRealValue();

		long electionRoundParam = electionRequest.getElectionRound();
		if (PaxosMemberStatus.NORMAL == currentMember.getStatus().get() && electionRoundSaved >= electionRoundParam) {
			/**
			 * 当前结点状态是已选举完成状态且当前结点保存的选举轮数要大于等于传入的参数轮数，则拒绝该提议
			 */
			logger.error(">==================electionData of firstPhase current member status is normal elected,param[" + logStr + "]");
			ElectionResponse response = RequestAndResponseUtil.composeFirstPahseElectionResponse(CodeInfo.DENY_CODE_FOR_HAS_LEADER,
					maxAcceptFirstPhaseNum, maxAcceptSecondPhaseNum, maxAcceptSecondPhaseValue, realNumSaved, realValueSaved,
					electionRoundSaved);
			return response;
		}

		ElectionResponse denyResponse = RequestAndResponseUtil.composeFirstPahseElectionResponse(CodeInfo.DENY_CODE,
				maxAcceptFirstPhaseNum, maxAcceptSecondPhaseNum, maxAcceptSecondPhaseValue, realNumSaved, realValueSaved,
				electionRoundSaved);

		/**
		 * 保存一阶段提议号，底层会检查请求提议号与已经保存的一阶段提议号是否合法
		 */
		long reqNum = electionRequest.getNum();
		ElectionResponse firstPhaseMaxNumSaveRes = paxosStore.saveAcceptFirstPhaseMaxNumForCurrentMember(reqNum);
		if (CodeInfo.DENY_CODE.equals(firstPhaseMaxNumSaveRes.getCode())) {
			/**
			 * 当前已经保存的提议号大于请求的提议号则回复deny
			 */
			logger.debug(">deny firstPhase election request,current maxAcceptFirstPhaseNum is bigger than num," + logStr
					+ ",maxAcceptFirstPhaseNum is[" + maxAcceptFirstPhaseNum + "]");

			return denyResponse;
		}

		/**
		 * 都符合条件最终返回接受信息
		 */
		return RequestAndResponseUtil.composeFirstPahseElectionResponse(CodeInfo.ACCEPT_CODE, maxAcceptFirstPhaseNum,
				maxAcceptSecondPhaseNum, maxAcceptSecondPhaseValue, realNumSaved, realValueSaved, electionRoundSaved);
	}

	/**
	 * 接收者接到二阶段请求
	 * 
	 * @param request
	 */
	@Override
	public ElectionResponse processElectionRequestSecondPhase(ElectionRequest request) {
		String logStr = JSON.toJSONString(request);
		PaxosMember currentMember = paxosStore.getCurrentPaxosMember();

		Long maxAcceptFirstPhaseNum = currentMember.getElectionInfo().getMaxAcceptFirstPhaseNum();
		Long maxAcceptSecondPhaseNum = currentMember.getElectionInfo().getMaxAcceptSecondPhaseNum();
		Object maxAcceptSecondPhaseValue = currentMember.getElectionInfo().getMaxAcceptSecondPhaseValue();
		ElectionResponse denyResponse = RequestAndResponseUtil.composeSecondPahseElectionResponse(CodeInfo.DENY_CODE,
				maxAcceptFirstPhaseNum, maxAcceptSecondPhaseNum, maxAcceptSecondPhaseValue);

		long electionRoundParam = request.getElectionRound();
		long currentElectionRound = currentMember.getElectionInfo().getElectionRound();
		if (PaxosMemberStatus.NORMAL == currentMember.getStatus().get() && currentElectionRound >= electionRoundParam) {
			/**
			 * 当前结点状态是已选举完成状态，则拒绝该提议，并且如果当前保存的已经选举成功的轮数大于等于传入的轮数，就告诉提议者当前结点已经选举完成
			 */
			logger.error(">==================electionData of secondPhase current member status is normal elected,param[" + logStr + "]");
			return denyResponse;
		}

		/**
		 * 如果请求号小于一阶段最大提议号则拒绝
		 */
		long reqNum = request.getNum();
		if (maxAcceptFirstPhaseNum != null && maxAcceptFirstPhaseNum > reqNum) {
			/**
			 * 当前已经保存的提议号大于请求的提议号则回复deny
			 */
			logger.debug(">==================deny secondPhase election request,current maxAcceptFirstPhaseNum is bigger than num," + logStr
					+ ",maxAcceptFirstPhaseNum is[" + maxAcceptFirstPhaseNum + "]");

			return denyResponse;
		}

		/**
		 * 全部成功后返回成功接受的消息
		 */
		return RequestAndResponseUtil.composeSecondPahseElectionResponse(CodeInfo.ACCEPT_CODE, maxAcceptFirstPhaseNum,
				maxAcceptSecondPhaseNum, maxAcceptSecondPhaseValue);
	}

	/**
	 * 接收到广播的选举结果
	 * 
	 * @param ctx
	 * @param request
	 */
	@Override
	public ElectionResultResponse processElectionResultRequest(ElectionResultRequest request) {
		String logStr = JSON.toJSONString(request);
		ElectionResultResponse failResponse = RequestAndResponseUtil.composeElectionResultResponse(CodeInfo.FAIL_CODE);
		if (request == null) {
			return failResponse;
		}

		long realNum = request.getNum();
		Object realValue = request.getValue();
		long electionRound = request.getElectionRound();
		boolean flag = this.processAfterElectionSecondPhase(true, electionRound, realNum, realValue, false);
		if (!flag) {
			logger.error(">==================invoke processAfterElectionSecondPhase of processElectionResultRequest err,param[" + logStr + "]");
			return failResponse;
		}

		/**
		 * 保存结果成功后，响应发送者成功
		 */
		logger.info(">succ save electionResult," + logStr);
		return RequestAndResponseUtil.composeElectionResultResponse(CodeInfo.SUCCESS_CODE);
	}

	private boolean processAfterElectionSecondPhase(boolean electionSuccessFlag, Long electionRound, Long realNum, Object realValue,
			boolean shouldSendResult) {
		String logStr = "electionRound[" + electionRound + "],realNum[" + realNum + "],realValue[" + realValue + "],electionSuccessFlag["
				+ electionSuccessFlag + "],shouldSendResult[" + shouldSendResult + "]";
		logger.info(">begin processAfterElectionSecondPhase," + logStr);

		if (!electionSuccessFlag) {
			/**
			 * 选举失败，设置当前结点状态为初始状态，下次可以再提议
			 */
			paxosStore.updateCurrentMemberStatus(null, PaxosMemberStatus.INIT);
			return false;
		}

		boolean saveElectionRes = paxosStore.saveElectionResultAndSetStatus(electionRound, realNum, realValue);
		if (!saveElectionRes) {
			logger.error(">==================processAfterElectionSecondPhase saveElectionResultAndSetStatus err," + logStr);
			return false;
		}

		return true;
	}

}
