package com.paxos.web.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.paxos.core.component.PaxosCoreComponent;
import com.paxos.core.domain.PaxosMember;
import com.paxos.protocal.task.HeartBeatProcessor;

@Controller
public class IndexController implements InitializingBean {

	@Autowired
	private PaxosCoreComponent paxosMemberService;

	@Autowired
	private HeartBeatProcessor heartBeatProcessor;

	private static final String GET_INFO_URL = "http://localhost:%s/getLeaderMember";

	private static Map<Integer/* tcp port */, Integer/* http serverPort */> map = new HashMap<Integer, Integer>();

	static {
		map.put(10051, 8081);
		map.put(10052, 8082);
		map.put(10053, 8083);
		map.put(10054, 8084);
		map.put(10055, 8085);
		map.put(10056, 8086);
		map.put(10057, 8087);
		map.put(10058, 8088);
		map.put(10059, 8089);
		map.put(10060, 8090);
	}

	public static void main(String[] args) {
		String s = String.format(GET_INFO_URL, 8087);
		System.out.println(s);
	}

	@RequestMapping("/getLeaderMember")
	@ResponseBody
	public String getLeaderMember() {
		PaxosMember currentMember = paxosMemberService.getCurrentPaxosMember();
		String res = "electionRound:," + currentMember.getElectionInfo().getElectionRound() + "========currentMember Name: "
				+ currentMember.getMemberName() + ",ip: " + currentMember.getIp() + ",port: " + currentMember.getPort() + ",isUp:"
				+ currentMember.getIsUp() + ",status:" + currentMember.getStatus();

		res += "\n";

		if (currentMember.getLeaderMember() == null) {
			return res;
		}
		String leaderIp = currentMember.getLeaderMember().getIp();
		int leaderPort = currentMember.getLeaderMember().getPort();
		res += "========================leader ip: " + leaderIp + " ,";
		res += ",port: " + leaderPort + ",isUp:" + currentMember.getLeaderMember().getIsUp() + ",status:"
				+ currentMember.getLeaderMember().getStatus();

		/**
		 * get all other member info
		 */
//		List<String> resInfoList = new ArrayList<String>();
//		List<PaxosMember> otherMemberList = paxosMemberService.getOtherPaxosMemberList();
//		for (PaxosMember paxosMember : otherMemberList) {
//			try {
//				int httpPort = map.get(paxosMember.getPort());
//				String resInfo = "====currentMember,ip[" + paxosMember.getIp() + "],port[" + paxosMember.getPort() + "]";
//				ExchangeClient exchangeClient = heartBeatProcessor.getExchangeClientReconnectWhenNotExist(paxosMember);
//				if (exchangeClient == null) {
//					resInfo += ",无法连接";
//				} else {
//					RemotingCommand reqcomCommand = RequestAndResponseUtil.composeSearchInfoRequest(RemotingRequest.getAndIncreaseReq());
//					Object sendRes = exchangeClient.sendSync(reqcomCommand);
//					if (sendRes != null) {
//						PaxosMember resMember = JSON.parseObject(sendRes.toString(), PaxosMember.class);
//						resInfo += ",isUp[" + resMember.getIsUp() + "],status[" + resMember.getStatus() + "]";
//
//						PaxosMember resLeader = resMember.getLeaderMember();
//						if (resLeader != null) {
//							resInfo += "=====================leaderInfo,ip[" + resLeader.getIp() + "],port[" + resLeader.getPort()
//									+ "],isUp[" + resLeader.getIsUp() + "],status[" + resLeader.getStatus() + "]";
//						}
//					}
//				}
//
//				resInfoList.add(resInfo);
//			} catch (Exception e) {
//				logger.error(">get otherMember info err", e);
//			}
//		}
//
//		res += resInfoList.toString();
		return res;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		System.out.println("hi=========");
	}

}
