package com.paxos.remoting;

import java.io.UnsupportedEncodingException;

import com.paxos.remoting.api.RemotingClient;
import com.paxos.remoting.api.RemotingCommand;
import com.paxos.remoting.io.IORemotingClient;

/**
 * 
 * @author sanfeng
 *
 */
public class IOClientTest {

	//public static void main(String[] args) {
	//	RemotingClient client = new IORemotingClient();
	//	client.connect("127.0.0.1", 8888);
	//
	//	RemotingCommand req = new RemotingCommand();
	//	req.setVersion((byte)10);
	//	req.setCommandType((byte)3);
	//	String body = "三丰、小强、佐哥、展白、paxos小组";
	//	req.setBody(body);
	//	try {
	//		req.setLength(body.getBytes("utf-8").length);
	//	} catch (UnsupportedEncodingException e) {
	//		e.printStackTrace();
	//	}
	//
	//	RemotingCommand res = (RemotingCommand)client.sendSync(req);
	//	System.out.println("业务方接收到结果：" + res);
	//	client.stop();
	//}
}
