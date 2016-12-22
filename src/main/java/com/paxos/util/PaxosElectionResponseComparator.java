package com.paxos.util;

import java.util.Comparator;

import com.paxos.core.domain.ElectionResponse;

public class PaxosElectionResponseComparator implements Comparator<ElectionResponse> {

	@Override
	public int compare(ElectionResponse m1, ElectionResponse m2) {
		if (m1.getElectionRound() > m2.getElectionRound()) {
			return 1;
		} else if (m1.getElectionRound() < m2.getElectionRound()) {
			return -1;
		}

		if (m1.getMaxAcceptNum() > m2.getMaxAcceptNum()) {
			return 1;
		} else if (m1.getMaxAcceptNum() < m2.getMaxAcceptNum()) {
			return -1;
		} else {
			return 0;
		}
	}

}
