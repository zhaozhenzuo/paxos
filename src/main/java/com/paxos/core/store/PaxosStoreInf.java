package com.paxos.core.store;

import java.util.List;

import com.paxos.core.domain.ElectionResponse;
import com.paxos.core.domain.PaxosMember;

/**
 * 提供paxos存储相关服务
 * 
 * @author shenxiu
 *
 */
public interface PaxosStoreInf {

	public ElectionResponse saveAcceptSecondPhaseMaxNumAndValueForCurrentMember(long electionRound, long num, Object value);

	/**
	 * 清空当前成员接收到的二阶段提议值
	 * 
	 * @return
	 */
	public boolean cleanAcceptSecondPhaseValueForCurrentMember();

	/**
	 * 获取当前成员
	 * 
	 * @return
	 */
	public PaxosMember getCurrentPaxosMember();

	/**
	 * 获取除当前成员外集群其它成员
	 * 
	 * @return
	 */
	public List<PaxosMember> getOtherPaxosMemberList();

	public void setCurrentPaxosMember(PaxosMember paxosMember);

	/**
	 * 保存当前成员接受到的最大提议号
	 * 
	 * @param otherMemberList
	 */
	public void setOtherPaxosMemberList(List<PaxosMember> otherMemberList);

	public ElectionResponse saveAcceptFirstPhaseMaxNumForCurrentMember(long num);

	public boolean saveElectionResultAndSetStatus(Long electionRound, Long realNum, Object realValue);

	public boolean saveFirstPhaseNumAndProposalRoundForProposer(Long electionRound, Long proposalRound, Long proposalNum);

	public boolean updateCurrentMemberStatus(Integer expectStatus, Integer status);

	public boolean updateCurrentMemberLeaderMember(PaxosMember leaderMember);

}
