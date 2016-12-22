package com.paxos.remoting.api;

import java.io.Serializable;

/**
 * 操作请求的响应
 * 
 * @author zhaozhenzuo
 *
 */
public class RemotingResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 响应id=8字
	 */
	private Long reqId;

	/**
	 * 响应类型,{@link com.paxos.core.constants.CodeInfo}<br/>
	 * 1字节
	 */
	private byte type;

	/**
	 * 响应结果
	 */
	private String data;

	public Long getReqId() {
		return reqId;
	}

	public void setReqId(Long reqId) {
		this.reqId = reqId;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

}
