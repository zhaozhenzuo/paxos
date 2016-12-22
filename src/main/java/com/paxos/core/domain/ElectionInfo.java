package com.paxos.core.domain;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

import com.paxos.core.constants.CodeInfo;

/**
 * 该paxos成员保存的选举提议号或者作为接收者接收到的最大提议号等<br/>
 * 注：对于接收者相关信息需要持久化，否则会造成集群不一致<br/>
 * 考虑以下情况：<br/>
 * 5个结点:a,b,c,d,e<br/>
 * 1.提议者a提示的提议编号为n,值为v1，已经二阶段被集群超过半数接受，设接受这个决议值v1的结点为a,b,c。 提议者a之后向leaner广播这个决议值v1<br/>
 * 2.这时a(提议者),b,c挂了，提议者a广播的v1值被e接受。<br/>
 * 3.之后a,b,c重启，因为之前的数据丢失，d马上发起一阶段：提议号n+1，提议值v2。 这时a,b,c会通过该提议(由于a,b,c,数据丢失)。d继续发起二阶段给a,b,c，同样被通过。<br/>
 * 4.通过后，d广播决议值v2给a,b,c。这三者都学习到该值<br/>
 * 最终集群中e保留了v1值，a,b,c保留了v2。整个集群就不一致了
 * 
 * 
 * @author zhaozhenzuo
 *
 */
public class ElectionInfo implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 表示第几轮选举,初始为0，每完成一轮选举加1,这个值必须由接受者保存
	 */
	private AtomicLong electionRound;

	/**
	 * 选举完成后是由哪个提议号决定的值
	 */
	private Long realNum;

	/**
	 * 选举完成后实际值,后面可优化，目前是针对选举
	 */
	private Object realValue;

	/***************** 提议者信息-begin **************************/

	/**
	 * 提议者当前提议第几次
	 */
	private AtomicLong proposalRound;

	/**
	 * 提议者自己当前的提议号
	 */
	private long proposalNumForProposer;

	/**
	 * 提议者一阶段时提示的值
	 */
	private Object firstPhaseValueForProposer;

	/**
	 * 提议者二阶段时提示的值
	 */
	private Object secondPhaseValueForProposer;

	/**
	 * 集群结点数
	 */
	private Integer clusterNodesNum;

	/**
	 * 当前结点提议时唯一序列号
	 */
	private Integer currentMemberUniqueProposalSeq;

	/***************** 提议者信息-end **************************/

	/***************** 接受者信息-begin **************************/

	/**
	 * 该接收者已经接收到一阶段的最大提议号
	 */
	private Long maxAcceptFirstPhaseNum;

	/**
	 * 该接收者已经accept第二阶段的最大提议号
	 */
	private Long maxAcceptSecondPhaseNum;

	/**
	 * 该接收者已经accept第二阶段最大提议号对应的value
	 */
	private Object maxAcceptSecondPhaseValue;

	/***************** 接受者信息-end **************************/

	public ElectionInfo() {
		this.proposalNumForProposer = CodeInfo.INIT_PROPOSAL_NUM;
		this.proposalRound = new AtomicLong(CodeInfo.INIT_PROPOSAL_ROUND);
		this.electionRound = new AtomicLong(CodeInfo.INIT_ELECTION_ROUND_NUM);
		this.maxAcceptFirstPhaseNum = CodeInfo.INIT_MAX_ACCEPT_FIRST_PHASE_NUM;
		this.maxAcceptSecondPhaseNum = CodeInfo.INIT_MAX_ACCEPT_SECOND_PHASE_NUM;
	}

	public Long getProposalNumForProposer() {
		return proposalNumForProposer;
	}

	public void setProposalNumForProposer(Long proposalNumForProposer) {
		this.proposalNumForProposer = proposalNumForProposer;
	}

	public Object getFirstPhaseValueForProposer() {
		return firstPhaseValueForProposer;
	}

	public void setFirstPhaseValueForProposer(Object firstPhaseValueForProposer) {
		this.firstPhaseValueForProposer = firstPhaseValueForProposer;
	}

	public Object getSecondPhaseValueForProposer() {
		return secondPhaseValueForProposer;
	}

	public void setSecondPhaseValueForProposer(Object secondPhaseValueForProposer) {
		this.secondPhaseValueForProposer = secondPhaseValueForProposer;
	}

	public long getElectionRound() {
		return electionRound.get();
	}

	public void setElectionRoundByValue(long roundValue) {
		electionRound.set(roundValue);
	}

	public Long getRealNum() {
		return realNum;
	}

	public void setRealNum(Long realNum) {
		this.realNum = realNum;
	}

	public Object getRealValue() {
		return realValue;
	}

	public void setRealValue(Object realValue) {
		this.realValue = realValue;
	}

	public long getProposalRound() {
		return proposalRound.get();
	}

	public void setProposalRoundByValue(long round) {
		proposalRound.set(round);
	}

	public long getAndIncreaseProposalRound() {
		return proposalRound.getAndIncrement();
	}

	public Integer getClusterNodesNum() {
		return clusterNodesNum;
	}

	public void setClusterNodesNum(Integer clusterNodesNum) {
		this.clusterNodesNum = clusterNodesNum;
	}

	public Integer getCurrentMemberUniqueProposalSeq() {
		return currentMemberUniqueProposalSeq;
	}

	public void setCurrentMemberUniqueProposalSeq(Integer currentMemberUniqueProposalSeq) {
		this.currentMemberUniqueProposalSeq = currentMemberUniqueProposalSeq;
	}

	public Long getMaxAcceptFirstPhaseNum() {
		return maxAcceptFirstPhaseNum;
	}

	public void setMaxAcceptFirstPhaseNum(Long maxAcceptFirstPhaseNum) {
		this.maxAcceptFirstPhaseNum = maxAcceptFirstPhaseNum;
	}

	public Long getMaxAcceptSecondPhaseNum() {
		return maxAcceptSecondPhaseNum;
	}

	public void setMaxAcceptSecondPhaseNum(Long maxAcceptSecondPhaseNum) {
		this.maxAcceptSecondPhaseNum = maxAcceptSecondPhaseNum;
	}

	public Object getMaxAcceptSecondPhaseValue() {
		return maxAcceptSecondPhaseValue;
	}

	public void setMaxAcceptSecondPhaseValue(Object maxAcceptSecondPhaseValue) {
		this.maxAcceptSecondPhaseValue = maxAcceptSecondPhaseValue;
	}

	public void setProposalNumForProposer(long proposalNumForProposer) {
		this.proposalNumForProposer = proposalNumForProposer;
	}

}
