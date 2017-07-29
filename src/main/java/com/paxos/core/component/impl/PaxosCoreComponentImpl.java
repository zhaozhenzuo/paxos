package com.paxos.core.component.impl;

import java.util.List;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.paxos.core.component.PaxosCoreComponent;
import com.paxos.core.constants.CodeInfo;
import com.paxos.core.domain.BaseElectionResponse;
import com.paxos.core.domain.ElectionInfo;
import com.paxos.core.domain.ElectionRequest;
import com.paxos.core.domain.ElectionResponse;
import com.paxos.core.domain.ElectionResultRequest;
import com.paxos.core.domain.PaxosMember;
import com.paxos.core.service.ElectionServiceForProposer;
import com.paxos.core.service.ElectionServiceForAcceptor;
import com.paxos.core.store.PaxosStoreInf;
import com.paxos.util.BizSerialAndDeSerialUtil;
import com.paxos.util.PaxosConflictionUtil;

/**
 * paxos核心对外api
 * 
 * @author zhaozhenzuo
 *
 */
@Component
public class PaxosCoreComponentImpl implements PaxosCoreComponent {
	
	@Autowired
	private ElectionServiceForProposer electionService;

	@Autowired
	private PaxosStoreInf paxosStoreInf;

	@Autowired
	private ElectionServiceForAcceptor electionServiceForAcceptor;

	@Override
	public PaxosMember getCurrentPaxosMember() {
		return paxosStoreInf.getCurrentPaxosMember();
	}

	@Override
	public List<PaxosMember> getOtherPaxosMemberList() {
		return paxosStoreInf.getOtherPaxosMemberList();
	}

	@Override
	public void setCurrentPaxosMember(PaxosMember paxosMember) {
		paxosStoreInf.setCurrentPaxosMember(paxosMember);
	}

	@Override
	public void setOtherPaxosMemberList(List<PaxosMember> otherMemberList) {
		paxosStoreInf.setOtherPaxosMemberList(otherMemberList);
	}

	@Override
	public ElectionResponse saveAcceptFirstPhaseMaxNumForCurrentMember(long num) {
		return paxosStoreInf.saveAcceptFirstPhaseMaxNumForCurrentMember(num);
	}

	@Override
	public ElectionInfo getElectionInfoForCurrentMember() {
		return getCurrentPaxosMember().getElectionInfo();
	}

	@Override
	public ElectionResponse saveAcceptSecondPhaseMaxNumAndValueForCurrentMember(long electionRound, long num, Object value) {
		return paxosStoreInf.saveAcceptSecondPhaseMaxNumAndValueForCurrentMember(electionRound, num, value);
	}

	@Override
	public boolean processAfterElectionSecondPhase(boolean electionSuccessFlag, Long electionRound, Long realNum, Object realValue,
			boolean shouldSendResultToOther) {
		return electionService.processAfterElectionSecondPhase(electionSuccessFlag, electionRound, realNum, realValue,
				shouldSendResultToOther);
	}

	@Override
	public BaseElectionResponse processElectionRequestForAcceptor(byte type, String electionRequestData) {
		BaseElectionResponse electionResponse;
		if (CodeInfo.REQ_TYPE_ELECTION_FIRST_PHASE == type || CodeInfo.REQ_TYPE_ELECTION_SECOND_PHASE == type) {
			ElectionRequest electionRequest = BizSerialAndDeSerialUtil.parseElectionRequest(electionRequestData);
			electionResponse = this.processElection(type, electionRequest);
		} else if (CodeInfo.REQ_TYPE_ELECTION_RESULT_TO_LEANER == type) {
			/**
			 * 接收选举结果
			 */
			ElectionResultRequest electionResultRequest = BizSerialAndDeSerialUtil.parseElectionResultRequest(electionRequestData);
			electionResponse = electionServiceForAcceptor.processElectionResultRequest(electionResultRequest);

			/**
			 * 选举成功后,之前的选举间隔时间调回来
			 */
			PaxosConflictionUtil.electionIntervalBetweenRound = PaxosConflictionUtil.DEFALT_INTERVAL;
		} else {
			throw new IllegalArgumentException("unsupport request bizType,type[" + type + "]," + electionRequestData);
		}

		return electionResponse;
	}

	private BaseElectionResponse processElection(byte type, ElectionRequest electionRequest) {
		BaseElectionResponse electionResponse;
		if (CodeInfo.REQ_TYPE_ELECTION_FIRST_PHASE == type) {
			electionResponse = electionServiceForAcceptor.processElectionRequestFirstPhase(electionRequest);
		} else if (CodeInfo.REQ_TYPE_ELECTION_SECOND_PHASE == type) {
			electionResponse = electionServiceForAcceptor.processElectionRequestSecondPhase(electionRequest);
		} else {
			throw new IllegalArgumentException("不支持的选举类型");
		}

		return electionResponse;

	}

}
