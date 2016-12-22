//package com.paxos.remoting;
//
//import org.springframework.util.Assert;
//
//import com.alibaba.fastjson.JSON;
//import com.paxos.core.constants.CodeInfo;
//import com.paxos.extrange.DefaultExChangeClient;
//import com.paxos.extrange.ExchangeClient;
//
//public class ClientTest {
//
//	public static void main(String[] args) throws Exception {
//		ExchangeClient exchangeClient=new DefaultExChangeClient();
//		boolean flag=exchangeClient.connect("192.168.124.4", 10051);
//		Assert.isTrue(flag);
//		
//		/**
//		 * 发送消息
//		 */
//		byte version=1;
//		byte type=1;
//		RemotingCommand command=new RemotingCommand(version,type);
//		
//		RemotingRequest request=new RemotingRequest(RemotingRequest.getAndIncreaseReq());
//		request.setType(CodeInfo.REQ_TYPE_HEART_BEAT);
//		request.setData("heartbeat request");
//		command.setBody(JSON.toJSONString(request));
//		
//		Object res=exchangeClient.sendSync(command);
//		
//		System.out.println("res="+res);
//	}
//	
//}
