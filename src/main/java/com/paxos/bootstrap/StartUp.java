package com.paxos.bootstrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.paxos.config.ElectionInfoLoader;
import com.paxos.config.PaxosConfig;
import com.paxos.core.component.PaxosCoreComponent;
import com.paxos.core.constants.CodeInfo;
import com.paxos.core.domain.ElectionInfo;
import com.paxos.core.domain.PaxosMember;
import com.paxos.enums.PaxosMemberRole;
import com.paxos.enums.PaxosMemberStatus;
import com.paxos.protocal.handler.UpStreamHandler;
import com.paxos.protocal.task.ElectionProcessor;
import com.paxos.protocal.task.HeartBeatProcessor;
import com.paxos.util.IPUtil;
import com.paxos.util.PaxosMemberComparator;

@Component
public class StartUp implements InitializingBean {

	private static final Logger logger = Logger.getLogger(StartUp.class);

	@Autowired
	private PaxosConfig paxosConfig;

	@Autowired
	private PaxosCoreComponent paxosMemberService;

	@Autowired
	private HeartBeatProcessor heartBeatProcessor;

	@Autowired
	private ElectionProcessor electionProcessor;

	@Autowired
	private ElectionInfoLoader electionInfoLoader;

	@Override
	public void afterPropertiesSet() throws Exception {
		/**
		 * 1.根据配置信息初始化member
		 */
		String ip = paxosConfig.getIp();

		// 选举端口
		int port = Integer.valueOf(paxosConfig.getPort());
		String nodesStr = paxosConfig.getClusterNodes();

		/**
		 * 2.心跳端口=选举端口+1
		 */
		int heartbeatPort = port + 1;
		paxosConfig.setHeartbeatPort(String.valueOf(heartbeatPort));

		/**
		 * 2.1.初始化member属性
		 */
		PaxosMember currentMember = this.initPaxosMember(ip, port, true);
		currentMember.setMemberName(paxosConfig.getMemberName());
		currentMember.setIsCurrentMember(true);
		List<PaxosMember> clusterMemberList = this.parseNodesToPaxosMember(ip, port, nodesStr);
		currentMember.setClusterMemberList(clusterMemberList);
		logger.info(">clusterMemberList is[" + JSON.toJSONString(clusterMemberList) + "]");

		/**
		 * 2.2.设置集群结点总数
		 */
		int clusterNodesNum = clusterMemberList != null ? clusterMemberList.size() : 0;
		currentMember.setClusterNodesNum(clusterNodesNum);
		logger.info(">clusterNodesNum is[" + clusterNodesNum + "]");

		/**
		 * 2.3.设置选举信息
		 */
		ElectionInfo electionInfo = new ElectionInfo();
		electionInfo.setClusterNodesNum(clusterNodesNum);
		int currentMemberUniqueProposalSeq = this.processProposalUniqueProposalSeq(clusterMemberList);
		electionInfo.setCurrentMemberUniqueProposalSeq(currentMemberUniqueProposalSeq);
		logger.info(">currentMemberUniqueProposalSeq is[" + currentMemberUniqueProposalSeq + "]");

		currentMember.setElectionInfo(electionInfo);

		/**
		 * 3.设置paxosMember到paxosMember服务中
		 */
		paxosMemberService.setCurrentPaxosMember(currentMember);
		paxosMemberService.setOtherPaxosMemberList(this.getOtherPaxMemberList(clusterMemberList));

		/**
		 * 3.1.load electionInfo from store
		 */
		electionInfoLoader.loadElectionInfo();

		/**
		 * 4.开启心跳处理器
		 */
		UpStreamHandler upStreamHandler = new UpStreamHandler(paxosMemberService);
		heartBeatProcessor.start(upStreamHandler);

		/**
		 * 5.开启选举处理器
		 */
		UpStreamHandler upStreamHandlerForElection = new UpStreamHandler(paxosMemberService);
		electionProcessor.start(upStreamHandlerForElection);

	}

	private List<PaxosMember> getOtherPaxMemberList(List<PaxosMember> clusterMemberList) {
		if (CollectionUtils.isEmpty(clusterMemberList)) {
			return null;
		}

		List<PaxosMember> otherMemberList = new ArrayList<PaxosMember>();
		for (PaxosMember paxosMember : clusterMemberList) {
			if (!paxosMember.getIsCurrentMember()) {
				otherMemberList.add(paxosMember);
			}
		}

		return otherMemberList;
	}

	private int processProposalUniqueProposalSeq(List<PaxosMember> clusterMemberList) {
		/**
		 * 1.检查是否集群结点为空或1个结点
		 */
		if (CollectionUtils.isEmpty(clusterMemberList) || clusterMemberList.size() == 1) {
			/**
			 * 集群中结点为空，或只有一个结点，提议序列唯一值可以取1
			 */
			return 1;
		}

		/**
		 * 2.有多个结点，将所有结点ip+port进行排序，设置每个结点一个提议时产生提议号的唯一提议序号标识
		 */
		Collections.sort(clusterMemberList, new PaxosMemberComparator());

		/**
		 * 3.排序完成后，只要返回当前结点的提议唯一序列即可
		 */
		Integer proposalUniqueSeqForCurrentMember = null;
		for (int i = 0; i < clusterMemberList.size(); i++) {
			PaxosMember paxosMember = clusterMemberList.get(i);
			logger.info(">member,ip[" + paxosMember.getIp() + "],port[" + paxosMember.getPort() + "],which proposalUniqueSeq is[" + i + "]");
			if (paxosMember.getIsCurrentMember()) {
				proposalUniqueSeqForCurrentMember = i;
			}
		}

		if (proposalUniqueSeqForCurrentMember == null) {
			throw new IllegalArgumentException("处理当前结点提议唯一序列时，没有找到,集群结点集合[" + clusterMemberList.toString() + "]");
		}

		return proposalUniqueSeqForCurrentMember;
	}

	private List<PaxosMember> parseNodesToPaxosMember(String currentIp, int currentPort, String nodes) throws Exception {
		if (StringUtils.isEmpty(nodes)) {
			return null;
		}

		List<PaxosMember> paxosMemberList = new ArrayList<PaxosMember>();
		try {
			String[] nodeArr = nodes.split(PaxosConfig.NODES_SPLIT);
			if (nodeArr == null || nodeArr.length <= 0) {
				return null;
			}

			boolean clusterContainsCurrentMember = false;

			for (String nodeStr : nodeArr) {
				String[] nodeIpAndPort = nodeStr.split(PaxosConfig.IP_PORT_SPLIT_FOR_NODES);
				if (nodeIpAndPort == null || nodeIpAndPort.length != 2) {
					throw new IllegalAccessException("集群结点nodes参数异常,结点格式为[ip:port]");
				}

				String ip = nodeIpAndPort[0];
				Integer port = IPUtil.parsePort(nodeIpAndPort[1]);
				logger.info(">found node,ip[" + ip + "],port[" + port + "]");

				/**
				 * 初始时，默认其它结点是有效的及状态为初始INIT状态
				 */
				PaxosMember paxosMember = this.initPaxosMember(ip, port, false);
				paxosMemberList.add(paxosMember);

				if (ip.equals(currentIp) && port == currentPort) {
					/**
					 * 这个结点就是当前结点，需要标识下
					 */
					logger.info(">nodes ip and port eq current ip and port,ignore,ip[" + ip + "],port[" + port + "]");
					clusterContainsCurrentMember = true;
					paxosMember.setIsCurrentMember(true);
				}

			}

			/**
			 * 将所有结点放入集群结点后，判断是否已经包含了当前结点不包含就抛错
			 */
			if (!clusterContainsCurrentMember) {
				throw new IllegalArgumentException("当前集群结点必须包含当前结点,当前结点ip[" + currentIp + "],当前结点port[" + currentPort + "],集群结点集合["
						+ nodeArr + "]");
			}

			return paxosMemberList;
		} catch (Exception e) {
			logger.error(">parseNodesToPaxosMember err", e);
			throw e;
		}

	}

	private PaxosMember initPaxosMember(String ip, int port, boolean isCurrentMember) {
		PaxosMember paxosMember = new PaxosMember();
		paxosMember.setIp(ip);
		paxosMember.setPort(port);
		paxosMember.setIsUp(true);
		paxosMember.setStatusValue(PaxosMemberStatus.INIT);
		paxosMember.setIsCurrentMember(isCurrentMember);
		paxosMember.setRole(PaxosMemberRole.UNKNOW);
		paxosMember.setIpAndPort(ip + CodeInfo.IP_AND_PORT_SPLIT + port);
		return paxosMember;
	}

}
