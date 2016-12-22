package com.paxos.remoting.api;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.fastjson.JSON;

/**
 * 远程通信消息
 * 
 * @author zhaozhenzuo
 *
 */
public class RemotingRequest implements Serializable {

	private static final long serialVersionUID = 1L;

	public static AtomicLong reqIncrement = new AtomicLong(0);

	/***** header-begin *******/

	private Long reqId;

	/**
	 * 操作类型,1位
	 */
	private byte type;

	/**
	 * 数据内容
	 */
	private String data;

	public RemotingRequest() {

	}

	public RemotingRequest(long reqId) {
		this.reqId = reqId;
	}

	public Long getReqId() {
		return reqId;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public static long getAndIncreaseReq() {
		return reqIncrement.getAndIncrement();
	}

	public void setReqId(Long reqId) {
		this.reqId = reqId;
	}

	public static RemotingRequest parseCommandToRemotingRequest(String bizReq) {
		return JSON.parseObject(bizReq, RemotingRequest.class);
	}
	
	public static RemotingResponse parseCommandToRemotingResponse(String bizReq) {
		return JSON.parseObject(bizReq, RemotingResponse.class);
	}

}
