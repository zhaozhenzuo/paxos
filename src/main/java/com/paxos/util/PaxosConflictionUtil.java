package com.paxos.util;

import java.util.Random;

public class PaxosConflictionUtil {

	public static final long DEFALT_INTERVAL = 1000;

	/**
	 * 每轮选举间隔时间,之后有随机
	 */
	public static volatile long electionIntervalBetweenRound = DEFALT_INTERVAL;

	public static final long MIN_ELECTION_INTERVAL_BETWEEN_ROUND_MILLSECONDS = 30000;

	public static final long MAX_ELECTION_INTERVAL_BETWEEN_ROUND_MILLSECONDS = 60000;

	public static final int RANDOM_MAX_ELECTION_INTERVAL_SECONDS = 30;

	public static long getRandomElectionIntervalTime() {
		Random random = new Random();
		long resInMillSeconds = random.nextInt(RANDOM_MAX_ELECTION_INTERVAL_SECONDS) * 1000
				+ MIN_ELECTION_INTERVAL_BETWEEN_ROUND_MILLSECONDS;

		long res = resInMillSeconds;
		if (res > MAX_ELECTION_INTERVAL_BETWEEN_ROUND_MILLSECONDS) {
			res = MAX_ELECTION_INTERVAL_BETWEEN_ROUND_MILLSECONDS;
		}

		if (res < MIN_ELECTION_INTERVAL_BETWEEN_ROUND_MILLSECONDS) {
			res = MIN_ELECTION_INTERVAL_BETWEEN_ROUND_MILLSECONDS;
		}

		return res;
	}

}
