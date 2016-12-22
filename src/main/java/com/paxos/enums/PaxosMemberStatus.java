package com.paxos.enums;

public interface PaxosMemberStatus {
	/**
	 * 初始状态
	 */
	public static final int INIT = 0;

	/**
	 * 选举中
	 */
	public static final int ELECTIONING = 1;

	/**
	 * 选举完成正常状态
	 */
	public static final int NORMAL = 2;

}
