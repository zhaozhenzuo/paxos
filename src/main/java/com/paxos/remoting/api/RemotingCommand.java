package com.paxos.remoting.api;

import java.io.Serializable;

import com.paxos.core.constants.CodeInfo;

/**
 * 远程通信指令消息定义
 * 
 * @author zhaozhenzuo
 *
 */
public class RemotingCommand implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 版本号,1字节
	 */
	private byte version;

	/**
	 * 该通信指令类型，请求或响应,1字节
	 */
	private byte commandType;

	/**
	 * 内容长度,4字节
	 */
	private Integer length;

	/******** header-end **********/

	/**
	 * 内容,封装业务信息，具体请求或响应
	 */
	private String body;

	public RemotingCommand() {

	}

	public RemotingCommand(byte version, byte type) {
		this.version = version;
		this.commandType = type;
	}

	public RemotingCommand(byte type) {
		this.version = CodeInfo.VERSION;
		this.commandType = type;
	}

	public byte getVersion() {
		return version;
	}

	public void setVersion(byte version) {
		this.version = version;
	}

	public byte getCommandType() {
		return commandType;
	}

	public void setCommandType(byte commandType) {
		this.commandType = commandType;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
	
	@Override
	public String toString() {
		return "version=" + version 
				+ ", commandType=" + commandType 
				+ ", length=" + length
				+ ", body=" + body;
	}

}
