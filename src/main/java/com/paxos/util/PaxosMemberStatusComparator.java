package com.paxos.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.paxos.core.domain.PaxosMember;

public class PaxosMemberStatusComparator implements Comparator<PaxosMember> {

	@Override
	public int compare(PaxosMember o1, PaxosMember o2) {
		if (o1.getIsUp()) {
			return -1;
		} else if (!o1.getIsUp()) {
			return 1;
		} else {
			return 0;
		}

	}

	//public static void main(String[] args) {
	//	List<PaxosMember> paxosMemberList = new ArrayList<PaxosMember>();
    //
	//	PaxosMember m1 = new PaxosMember();
	//	m1.setIp("1");
	//	m1.setIsUp(true);
	//	paxosMemberList.add(m1);
    //
	//	PaxosMember m2 = new PaxosMember();
	//	m2.setIp("2");
	//	m2.setIsUp(false);
	//	paxosMemberList.add(m2);
    //
	//	PaxosMember m3 = new PaxosMember();
	//	m3.setIp("3");
	//	m3.setIsUp(true);
	//	paxosMemberList.add(m3);
	//
	//	PaxosMember m4 = new PaxosMember();
	//	m4.setIp("4");
	//	m4.setIsUp(true);
	//	paxosMemberList.add(m4);
    //
	//	Collections.sort(paxosMemberList, new PaxosMemberStatusComparator());
    //
	//	System.out.println(paxosMemberList.toString());
    //
	//	for (PaxosMember paxosMember : paxosMemberList) {
	//		System.out.println(paxosMember.getIp() + "," + paxosMember.getIsUp());
	//	}
	//}

}
