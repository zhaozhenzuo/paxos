package com.paxos.web.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.paxos.core.component.PaxosCoreComponent;
import com.paxos.core.domain.PaxosMember;
import com.paxos.protocal.task.HeartBeatProcessor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController implements InitializingBean {

	@Autowired
	private PaxosCoreComponent paxosMemberService;

	@Autowired
	private HeartBeatProcessor heartBeatProcessor;

	private static final String GET_INFO_URL = "http://localhost:%s/getLeaderMember";

	private static Map<Integer/* tcp port */, Integer/* http serverPort */> map = new HashMap<Integer, Integer>();

	private static Map<Integer, Integer> cache = new ConcurrentHashMap<>();

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

	@RequestMapping("/index")
	@ResponseBody
	public String index(@RequestParam(required = false) int i) {
		int res=fibonaacci(i);

		return "index page";
	}

	public int fibonaacci(int i) {
		if (i == 0 || i == 1) {
			return i;
		}
		// Java 8 Map接口中新增方法
		// 首先判断缓存MAP中是否存在指定key的值，如果不存在，会自动调用mappingFunction(key)
		// 计算key的value，然后将key = value
		// 放入到缓存Map,java8会使用thread-safe的方式从cache中存取记录。
		return cache.computeIfAbsent(i, (key) -> {
			// 函数式接口,key = i,就是传过来的key,函数中的第一个参数
			System.out.println("Compute fibonaacci " + key);
			return fibonaacci(key-1) + fibonaacci(key-2);
		});
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
