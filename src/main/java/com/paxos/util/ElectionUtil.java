package com.paxos.util;

import java.util.List;

import com.paxos.core.domain.PaxosMember;
import com.paxos.enums.PaxosMemberRole;
import com.paxos.enums.PaxosMemberStatus;

public class ElectionUtil {

	public static void fillLeaderToMemberList(PaxosMember currentMember, List<PaxosMember> otherMemberList,
			String electionRes) {
		String currentIpAndPort = currentMember.getIpAndPort();
		if (currentIpAndPort.equals(electionRes)) {
			currentMember.setRole(PaxosMemberRole.LEADER);
			currentMember.setLeaderMember(currentMember);
		}

		/**
		 * 选举结果leader是另外一个结点，则找到这个member设置为leaderMember
		 */
		for (PaxosMember paxosMember : otherMemberList) {
			if (paxosMember.getIpAndPort().equals(electionRes)) {
				paxosMember.setRole(PaxosMemberRole.LEADER);
				paxosMember.setStatusValue(PaxosMemberStatus.NORMAL);
				currentMember.setLeaderMember(paxosMember);
				break;
			}
		}
	}

}
