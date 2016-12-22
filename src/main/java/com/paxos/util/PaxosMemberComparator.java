package com.paxos.util;

import java.util.Comparator;

import com.paxos.core.domain.PaxosMember;

public class PaxosMemberComparator implements Comparator<PaxosMember> {

	@Override
	public int compare(PaxosMember m1, PaxosMember m2) {
		long ipAndPortLongM1 = IPUtil.ipToLong(m1.getIp()) + m1.getPort();
		long ipAndPortLongM2 = IPUtil.ipToLong(m2.getIp()) + m2.getPort();

		if (ipAndPortLongM1 > ipAndPortLongM2) {
			return 1;
		} else if (ipAndPortLongM1 < ipAndPortLongM2) {
			return -1;
		} else {
			return 0;
		}
	}

}
