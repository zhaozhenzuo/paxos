package com.paxos.core.component;

import java.util.List;

import com.paxos.core.domain.BaseElectionResponse;
import com.paxos.core.domain.ElectionInfo;
import com.paxos.core.domain.ElectionResponse;
import com.paxos.core.domain.PaxosMember;

/**
 * paxos核心对外api
 * 
 * @author zhaozhenzuo
 *
 */
public interface PaxosCoreComponent {

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

	public ElectionResponse saveAcceptSecondPhaseMaxNumAndValueForCurrentMember(long round, long num, Object value);

	public ElectionInfo getElectionInfoForCurrentMember();

	/**
	 * 处理二阶段选举完成后，广播选举结果
	 * 
	 * @param electionSuccessFlag
	 * @param electionRound
	 *            当前选举轮次
	 * @param realNum
	 *            促成选举结果的提议号
	 * @param realValue
	 *            选举结果
	 * @return
	 */
	public boolean processAfterElectionSecondPhase(boolean electionSuccessFlag, Long electionRound, Long realNum, Object realValue,
			boolean shouldSendResultToOther);

	/****************** 接收者处理选举请求接口-begin *******************************/

	/**
	 * 处理选举请求含接收一阶段请求,接收阶二请求及接收广播选举结果，内部会根据业务类型type进行分发s
	 * 
	 * @param type
	 *            {@link com.tongbanjie.rich.util.CodeInfo.REQ_TYPE_ELECTION_FIRST_PHASE}, //一阶段请求type
	 *            {@link com.tongbanjie.rich.util.CodeInfo.REQ_TYPE_ELECTION_SECOND_PHASE},//二阶段请求type
	 *            {@link com.tongbanjie.rich.util.CodeInfo.REQ_TYPE_ELECTION_RESULT_TO_LEANER},//广播选举结果给学习者请求type
	 */
	public BaseElectionResponse processElectionRequestForAcceptor(byte type, String electionRequestData);

	/****************** 接收者处理选举请求接口-end *******************************/

}
