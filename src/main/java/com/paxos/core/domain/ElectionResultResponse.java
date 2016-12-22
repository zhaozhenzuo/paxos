package com.paxos.core.domain;


public class ElectionResultResponse extends BaseElectionResponse {

	private static final long serialVersionUID = 1L;

	/**
	 * 状态码,-1-失败,1-成功
	 */
	private Integer code;

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

}
