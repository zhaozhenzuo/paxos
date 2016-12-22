package com.paxos.core.domain;

import java.io.Serializable;

/**
 * 父类请求
 * 
 * @author shenxiu
 *
 */
public class BaseRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 选举轮数
	 */
	private Long electionRound;

	public Long getElectionRound() {
		return electionRound;
	}

	public void setElectionRound(Long electionRound) {
		this.electionRound = electionRound;
	}

}
