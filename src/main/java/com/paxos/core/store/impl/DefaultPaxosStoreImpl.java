package com.paxos.core.store.impl;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.paxos.core.constants.CodeInfo;
import com.paxos.core.domain.ElectionInfo;
import com.paxos.core.domain.ElectionResponse;
import com.paxos.core.domain.PaxosMember;
import com.paxos.core.store.PaxosStoreInf;
import com.paxos.datastore.DataStoreInf;
import com.paxos.enums.PaxosMemberStatus;
import com.paxos.util.BizSerialAndDeSerialUtil;
import com.paxos.util.ElectionUtil;

@Repository
public class DefaultPaxosStoreImpl implements PaxosStoreInf {

	private static final Logger logger = Logger.getLogger(DefaultPaxosStoreImpl.class);

	private PaxosMember currentMember;

	private List<PaxosMember> otherMemberList;

	private Lock lock = new ReentrantLock();

	@Autowired
	private DataStoreInf dataStore;

	@Override
	public ElectionResponse saveAcceptFirstPhaseMaxNumForCurrentMember(long num) {
		lock.lock();
		try {
			ElectionInfo electionInfo = currentMember.getElectionInfo();
			if (electionInfo == null) {
				throw new IllegalArgumentException("当前成员electionInfo不能为空");
			}

			long maxProposalNumForAcceptor = electionInfo.getMaxAcceptFirstPhaseNum();
			long maxAcceptNumForAcceptor = electionInfo.getMaxAcceptSecondPhaseNum();
			Object maxAcceptValueForAcceptor = electionInfo.getMaxAcceptSecondPhaseValue();
			if (num < maxProposalNumForAcceptor) {
				logger.warn(">current maxAcceptFirstPahseNum is[" + maxProposalNumForAcceptor + "],and num is [" + num
						+ "],so give up this election");
				return new ElectionResponse(CodeInfo.DENY_CODE, maxProposalNumForAcceptor, maxAcceptNumForAcceptor,
						maxAcceptValueForAcceptor);
			}

			electionInfo.setMaxAcceptFirstPhaseNum(num);

			/**
			 * 持久化
			 */
			byte[] res = BizSerialAndDeSerialUtil.objectToBytesByByJson(electionInfo);
			dataStore.writeToStore(res);
			return new ElectionResponse(CodeInfo.ACCEPT_CODE, maxProposalNumForAcceptor, maxAcceptNumForAcceptor, maxAcceptValueForAcceptor);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public ElectionResponse saveAcceptSecondPhaseMaxNumAndValueForCurrentMember(long electionRoundParam, long num, Object value) {
		lock.lock();
		try {
			ElectionInfo electionInfo = currentMember.getElectionInfo();
			if (electionInfo == null) {
				throw new IllegalArgumentException("当前成员electionInfo不能为空");
			}

			long maxProposalNumForAcceptor = electionInfo.getMaxAcceptFirstPhaseNum();
			long maxAcceptNumOfSendPhaseForAcceptor = electionInfo.getMaxAcceptSecondPhaseNum();
			Object maxAcceptValueOfSendPhaseForAcceptor = electionInfo.getMaxAcceptSecondPhaseValue();
			if (electionRoundParam < electionInfo.getElectionRound()) {
				logger.warn(">electionRoundParam less than current electionRound,current electionRound is["
						+ electionInfo.getElectionRound() + "],and electionRoundParam is [" + electionRoundParam
						+ "],so give up this election");
				return new ElectionResponse(CodeInfo.DENY_CODE, maxProposalNumForAcceptor, maxAcceptNumOfSendPhaseForAcceptor,
						maxAcceptValueOfSendPhaseForAcceptor);
			}

			/**
			 * 是否小于已经接收到的一阶段最大提议号
			 */
			if (num < maxProposalNumForAcceptor) {
				logger.warn(">num less than MaxProposalNumForAcceptor,MaxProposalNumForAcceptor is[" + maxProposalNumForAcceptor
						+ "],and num is [" + num + "],so give up this election");
				return new ElectionResponse(CodeInfo.DENY_CODE, maxProposalNumForAcceptor, maxAcceptNumOfSendPhaseForAcceptor,
						maxAcceptValueOfSendPhaseForAcceptor);
			}

			/**
			 * 是否小于已经接收到的二阶段最大提议号
			 */
			if (num < electionInfo.getMaxAcceptSecondPhaseNum()) {
				logger.warn(">num less than MaxAcceptNumForAcceptor,MaxAcceptNumForAcceptor is["
						+ electionInfo.getMaxAcceptSecondPhaseNum() + "],and num is [" + num + "],so give up this election");
				return new ElectionResponse(CodeInfo.DENY_CODE, maxProposalNumForAcceptor, maxAcceptNumOfSendPhaseForAcceptor,
						maxAcceptValueOfSendPhaseForAcceptor);
			}

			electionInfo.setMaxAcceptSecondPhaseNum(num);
			electionInfo.setMaxAcceptSecondPhaseValue(value);

			/**
			 * 持久化
			 */
			byte[] res = BizSerialAndDeSerialUtil.objectToBytesByByJson(electionInfo);
			dataStore.writeToStore(res);
			return new ElectionResponse(CodeInfo.ACCEPT_CODE, maxProposalNumForAcceptor, num, value);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public PaxosMember getCurrentPaxosMember() {
		return currentMember;
	}

	@Override
	public List<PaxosMember> getOtherPaxosMemberList() {
		return otherMemberList;
	}

	@Override
	public void setCurrentPaxosMember(PaxosMember paxosMember) {
		this.currentMember = paxosMember;
	}

	@Override
	public void setOtherPaxosMemberList(List<PaxosMember> otherMemberList) {
		this.otherMemberList = otherMemberList;
	}

	public boolean saveElectionResultAndSetStatus(Long electionRound, Long realNum, Object realValue) {
		lock.lock();
		try {
			String logStr = "electionRound[" + electionRound + "],realNum[" + realNum + "],realValue[" + realValue + "]";

			/**
			 * 超过半数，则进行先保存当前结点实际的选举结果值
			 */
			ElectionInfo electionInfo = currentMember.getElectionInfo();
			if (electionInfo == null) {
				logger.error(">saveRealValueAndNum err,not found electionInfo," + logStr);
				return false;
			}

			/**
			 * 保存选举结果时要求当前的选举轮数必须小于等于传入的参数选举轮数
			 */
			if (electionInfo.getElectionRound() > electionRound) {
				logger.error(">saveRealValueAndNum err,current round larger than param,current round[" + electionInfo.getElectionRound()
						+ "]" + logStr);
				return false;
			}

			electionInfo.setRealNum(realNum);
			electionInfo.setRealValue(realValue);
			electionInfo.setElectionRoundByValue(electionRound);

			// 重置提议号及提议者提议的次数为初始值
//			electionInfo.setProposalNumForProposer(CodeInfo.INIT_PROPOSAL_NUM);
//			electionInfo.setProposalRoundByValue(CodeInfo.INIT_PROPOSAL_ROUND);

			/**
			 * 设置当前结点状态为已选举完成
			 */
			this.updateCurrentMemberStatus(null, PaxosMemberStatus.NORMAL);

			/**
			 * 设置当前二阶段接收值为null,下次重新选举时可以接受新值
			 */
			electionInfo.setMaxAcceptSecondPhaseValue(null);

			/**
			 * 看选举结果值是否是当前结点，是的话，设置当前结点角色为leader
			 */
			this.fillLeaderMember(realValue.toString());

			/**
			 * 持久化
			 */
			byte[] res = BizSerialAndDeSerialUtil.objectToBytesByByJson(electionInfo);
			dataStore.writeToStore(res);
			return true;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 找出leader结点
	 * 
	 * @param electionRes
	 * @return
	 */
	private void fillLeaderMember(String electionRes) {
		ElectionUtil.fillLeaderToMemberList(currentMember, otherMemberList, electionRes);
	}

	@Override
	public boolean saveFirstPhaseNumAndProposalRoundForProposer(Long electionRound, Long proposalRound, Long proposalNum) {
		if (proposalRound == null || proposalNum == null) {
			return false;
		}

		ElectionInfo electionInfo = getCurrentPaxosMember().getElectionInfo();
		long electionRoundOfCurrent = electionInfo.getElectionRound();
		if (electionRoundOfCurrent > electionRound) {
			logger.error(">current election is larger than param electionRound,current electionRound[" + electionRoundOfCurrent
					+ "],param electionRound[" + electionRound + "]");
			return false;
		}

		long proposalNumOfCurrent = electionInfo.getProposalNumForProposer();
		if (proposalNumOfCurrent > proposalNum) {
			logger.error(">current proposalNum is larger than param proposalNum,current proposalNumOfCurrent[" + proposalNumOfCurrent
					+ "],param proposalNum[" + proposalNum + "]");
			return false;
		}

		long proposalRoundOfCurrent = electionInfo.getProposalRound();
		if (proposalRoundOfCurrent > proposalRound) {
			logger.error(">current proposalRound is larger than param proposalRound,current proposalRoundOfCurrent["
					+ proposalRoundOfCurrent + "],param proposalRound[" + proposalRound + "]");
			return false;
		}

		lock.lock();
		try {
			electionInfo.setProposalRoundByValue(proposalRound);
			electionInfo.setProposalNumForProposer(proposalNum);
			return true;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean cleanAcceptSecondPhaseValueForCurrentMember() {
		lock.lock();

		try {
			ElectionInfo electionInfo = getCurrentPaxosMember().getElectionInfo();
			electionInfo.setMaxAcceptSecondPhaseValue(null);
			return true;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean updateCurrentMemberStatus(Integer expectStatus, Integer updateStatus) {
		lock.lock();
		try {

			if (updateStatus == null) {
				throw new IllegalArgumentException("要更新状态不能为空");
			}

			int oldStatus = currentMember.getStatus().get();
			if (expectStatus != null && oldStatus != expectStatus) {
				//logger.error(">updateCurrentMemberStatus err,oldStatus not eq expectStatus,expectStatus[" + expectStatus + "],oldStatus["
				//		+ oldStatus + "],updateStatus[" + updateStatus + "]");
				return false;
			}

			getCurrentPaxosMember().setStatusValue(updateStatus);
			return true;
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean updateCurrentMemberLeaderMember(PaxosMember leaderMember) {
		lock.lock();
		try {
			getCurrentPaxosMember().setLeaderMember(leaderMember);
			return true;
		} finally {
			lock.unlock();
		}
	}
}
