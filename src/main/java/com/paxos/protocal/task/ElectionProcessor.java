package com.paxos.protocal.task;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.paxos.core.component.PaxosCoreComponent;
import com.paxos.core.domain.PaxosMember;
import com.paxos.core.service.ElectionServiceForProposer;
import com.paxos.core.store.PaxosStoreInf;
import com.paxos.enums.PaxosMemberRole;
import com.paxos.enums.PaxosMemberStatus;
import com.paxos.protocal.handler.UpStreamHandler;
import com.paxos.util.ElectionNumberGenerator;
import com.paxos.util.PaxosConflictionUtil;

/**
 * 选举处理器<br/>
 * 当前结点如果状态不是处于［正常］已选举完成状态，在一段时间后需要发起提议
 * 
 * @author zhaozhenzuo
 *
 */
@Component
public class ElectionProcessor {

	private static final Logger logger = Logger.getLogger(ElectionProcessor.class);

	/**
	 * 默认定时时间
	 */
	private static final long DEFAULT_INTERVEL = 1000;

	/**
	 * 选举超时时间，超过这个时间该结点才重新生成提议号发起下次提议,30秒
	 */
	private static final long ELECTION_TIME_OUT = 5 * 6 * 1000;

	private ScheduledExecutorService scheduledExecutorService;

	@Autowired
	private PaxosCoreComponent memberService;

	@Autowired
	private ElectionServiceForProposer electionService;

	private volatile long lastTimeProposal;

	private Lock lock = new ReentrantLock();

	@Autowired
	private PaxosStoreInf paxosStore;

	public ElectionProcessor() {

	}

	public void start(UpStreamHandler upStreamHandler) {
		/**
		 * 0.检查
		 */
		if (upStreamHandler == null) {
			throw new IllegalArgumentException("upstreamHandler不能为空");
		}

		/**
		 * 2.开启定时查看与leader连接状态,如果连不上了，则进行选举
		 */
		scheduledExecutorService = Executors.newScheduledThreadPool(1);
		scheduledExecutorService.scheduleWithFixedDelay(new ElectionCheckTask(), 1000, DEFAULT_INTERVEL, TimeUnit.MILLISECONDS);

	}

	class ElectionCheckTask implements Runnable {

		@Override
		public void run() {
			logger.debug(">begin election check");

			/**
			 * 1.开始处理结点的status状态，两个方面<br/>
			 * a.当前结点非leader，则在其有leader情况下需要判断leader是否连的通，连不通则将当前结点状态置为INIT，以例进行选举<br/>
			 * b.当前是leader，则需要判断与其它结点是否连得上，连不上就设置当前leader状态为INIT
			 * 
			 */
			PaxosMember currentMember = memberService.getCurrentPaxosMember();
			if (currentMember.getRole() == PaxosMemberRole.LEADER) {
				processStatusForLeader();
			} else {
				processStatusForNotLeader();
			}

			/**
			 * 2.处理选举
			 */
			long electionIntervalBetweenRound = PaxosConflictionUtil.electionIntervalBetweenRound;
			logger.debug("================================next electionInterval[" + electionIntervalBetweenRound + "]");
			if (currentMember.getRole() == PaxosMemberRole.LEADER) {
				/**
				 * 当前结点是leader的话不需要主动发起选举
				 */
				return;
			}
			processElection();

			logger.debug(">end election check");
		}

	}

	private void processStatusForLeader() {
		PaxosMember currentMember = memberService.getCurrentPaxosMember();
		if (currentMember.getRole() != PaxosMemberRole.LEADER) {
			return;
		}

		List<PaxosMember> otherMemberList = paxosStore.getOtherPaxosMemberList();
		if (CollectionUtils.isEmpty(otherMemberList)) {
			return;
		}

		int liveMemberNum = 0;
		for (PaxosMember paxosMember : otherMemberList) {
			if (paxosMember.getIsUp()) {
				liveMemberNum++;
			}
		}

		if (liveMemberNum == 0) {
			/**
			 * 与其它结点都连不上，则设置当前leader结点状态为INIT，准备下一轮选举
			 */
			logger.info(">processForLeader leader cannot connect all other members,so set the status to init");
			paxosStore.updateCurrentMemberStatus(null, PaxosMemberStatus.INIT);
		}
	}

	private void processStatusForNotLeader() {
		PaxosMember currentMember = paxosStore.getCurrentPaxosMember();
		PaxosMember leaderMember = currentMember.getLeaderMember();
		if (leaderMember == null || !leaderMember.getIsUp()) {
			/**
			 * 不能连通leader
			 */
			paxosStore.updateCurrentMemberStatus(PaxosMemberStatus.NORMAL, PaxosMemberStatus.INIT);
			return;
		}

		if (leaderMember != null && leaderMember.getIsUp()) {
			/**
			 * 能连通leader
			 */
			paxosStore.updateCurrentMemberStatus(null, PaxosMemberStatus.NORMAL);
		}
	}

	private void processElection() {
		long curTime = new Date().getTime();
		if ((curTime - lastTimeProposal) < PaxosConflictionUtil.electionIntervalBetweenRound) {
			/**
			 * 未到下轮选举时间
			 */
			return;
		}

		/**
		 * 当前结点如果是选举中则返回，并且未超时
		 */
		PaxosMember currentMember = memberService.getCurrentPaxosMember();
		if (currentMember.getStatus().intValue() == PaxosMemberStatus.ELECTIONING && ((curTime - lastTimeProposal) < ELECTION_TIME_OUT)) {
			/**
			 * 该结点处于选举中，且未超时，则不再进行选举
			 */
			return;

		}

		if (currentMember.getLeaderMember() == null || !currentMember.getLeaderMember().getIsUp()) {
			/**
			 * leader不存在，或不可用，进行选举
			 */
			beginProposal();
		}
	}

	private void beginProposal() {
		lock.lock();

		long oldTime = System.currentTimeMillis();
		try {

			logger.info(">begin proposal in task");

			/**
			 * 0.将当前结点状态改为选举中
			 */
			PaxosMember currentMember = memberService.getCurrentPaxosMember();
			currentMember.setStatusValue(PaxosMemberStatus.ELECTIONING);

			int clusterMemberNum = currentMember.getClusterNodesNum();
			int currentMemberUniqueProposalSeq = currentMember.getElectionInfo().getCurrentMemberUniqueProposalSeq();
			long currentProposalRound = currentMember.getElectionInfo().getAndIncreaseProposalRound();
			long num = ElectionNumberGenerator.getElectionNumberByParam(clusterMemberNum, currentMemberUniqueProposalSeq,
					currentProposalRound);

			Object value = currentMember.getIpAndPort();

			lastTimeProposal = new Date().getTime();

			/**
			 * 一阶段提交
			 */
			long electionRound = currentMember.getElectionInfo().getElectionRound();
			// 提议时使用当前已选举轮数+1
			electionRound = electionRound + 1;
			String logStr = "electionRound[" + electionRound + "],num[" + num + "],value[" + value + "]";
			logger.info(">======1.begin proposalFirstPhase," + logStr);

			Pair<Boolean, Object> firstPhaseRes = electionService.proposalFirstPhase(electionRound, num, value);
			if (!firstPhaseRes.getValue0()) {
				PaxosConflictionUtil.electionIntervalBetweenRound = PaxosConflictionUtil.getRandomElectionIntervalTime();
				logger.info(">==========election be rejected in firstPhase," + logStr);
				paxosStore.updateCurrentMemberStatus(PaxosMemberStatus.ELECTIONING, PaxosMemberStatus.INIT);
				return;
			}
			logger.info(">======1.end proposalFirstPhase,result is[" + firstPhaseRes.getValue0() + "],param:" + logStr);

			/**
			 * 二阶段提交
			 */
			logger.info(">======2.begin proposalSecondPhase," + logStr);
			Object valueForSecondPhase = firstPhaseRes.getValue1();
			boolean secondPhaseRes = electionService.proposalSecondPhase(electionRound, num, valueForSecondPhase);
			if (!secondPhaseRes) {
				logger.info(">==========election be rejected in secondPhase," + logStr);
				paxosStore.updateCurrentMemberStatus(PaxosMemberStatus.ELECTIONING, PaxosMemberStatus.INIT);
				return;
			}
			logger.info(">======2.end proposalSecondPhase,result is[" + secondPhaseRes + "],param:" + logStr);

			/**
			 * 处理二阶段结果
			 */
			electionService.processAfterElectionSecondPhase(secondPhaseRes, electionRound, num, valueForSecondPhase, true);

			/**
			 * 选举成功后,之前的选举间隔时间调回来
			 */
			PaxosConflictionUtil.electionIntervalBetweenRound = PaxosConflictionUtil.DEFALT_INTERVAL;

			long cost = System.currentTimeMillis() - oldTime;

			String log = "electionRound[" + electionRound + "],realNum[" + num + "],realValue[" + valueForSecondPhase + "]," + "host["
					+ currentMember.getIp() + "],port[" + currentMember.getPort() + "],election finish,cost[" + cost + "],";
			writeLog(log);
		} finally {
			lock.unlock();
		}

		logger.info(">end proposal in task");
	}

	private static void writeLog(String data) {
		RandomAccessFile accessFile = null;
		try {
			accessFile = new RandomAccessFile("d:\\paxos\\logs\\out.log", "rw");
			long length = accessFile.length();
			accessFile.seek(length);

			accessFile.writeBytes(data + "\n");
		} catch (Exception e) {
			logger.error(">log result err", e);
		} finally {
			if (accessFile != null) {
				try {
					accessFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public static void main(String[] args) {
		writeLog("h3 cost");
		writeLog("h4 cost");
	}

	public void stop() {
		scheduledExecutorService.shutdownNow();
	}

	public long getLastTimeProposal() {
		return lastTimeProposal;
	}

	public void setLastTimeProposal(long lastTimeProposal) {
		this.lastTimeProposal = lastTimeProposal;
	}

}
