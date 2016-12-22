package com.paxos.enums;

public interface PaxosMemberRole {

	/**
	 * 未选举完成时的角色是不确定的
	 */
	public static final int UNKNOW = -1;

	public static final int LEADER = 1;

	public static final int FOLLOWER = 2;
	

}
