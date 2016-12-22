package com.paxos.core.domain;


/**
 * 选举结果发送请求<br/>
 * 
 * @author zhaozhenzuo
 *
 */
public class ElectionResultRequest extends BaseElectionRequest {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 当前选举轮数
	 */
	private Long electionRound;

	/**
	 * 当前选举结果的提议号
	 */
	private Long num;

	/**
	 * 当前选举结果
	 */
	private Object value;

	public Long getElectionRound() {
		return electionRound;
	}

	public void setElectionRound(Long electionRound) {
		this.electionRound = electionRound;
	}

	public Long getNum() {
		return num;
	}

	public void setNum(Long num) {
		this.num = num;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
	
}
