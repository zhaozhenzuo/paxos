package com.paxos.util;

import org.javatuples.Pair;

/**
 * 选举编号
 * 
 * @author zhaozhenzuo
 *
 */
public class ElectionNumberGenerator {

	public static final String SPLIT = ":";

	public static long getElectionNumberByParam(int clusterMemberNum, int currentMemberUniqueProposalSeq, long currentProposalRound) {
		long num = (currentProposalRound - 1) * clusterMemberNum + currentMemberUniqueProposalSeq;
		return num;
	}

	public static Pair<Long/* currentProposalRound */, Long/* electionNumer */> getElectionNumberByMaxNumber(final int clusterMemberNum,
			final int currentMemberUniqueProposalSeq, final long currentProposalRound, final long paramElectionNumer) {

		long resElectionNumber = getElectionNumberByParam(clusterMemberNum, currentMemberUniqueProposalSeq, currentProposalRound);
		long proposalRound = currentProposalRound;
		while (resElectionNumber < paramElectionNumer) {
			proposalRound += 1;
			resElectionNumber = getElectionNumberByParam(clusterMemberNum, currentMemberUniqueProposalSeq, proposalRound);
		}

		return new Pair<Long, Long>(proposalRound, resElectionNumber);
	}

	public static void main(String[] args) {
		int clusterMemberNum = 5;
		int currentMemberUniqueProposalSeq = 1;
		long currentProposalRound = 3;

		long res = ElectionNumberGenerator.getElectionNumberByParam(clusterMemberNum, currentMemberUniqueProposalSeq, currentProposalRound);
		System.out.println(res);
	}

}
