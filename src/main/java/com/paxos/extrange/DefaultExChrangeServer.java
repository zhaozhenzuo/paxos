package com.paxos.extrange;

import com.paxos.protocal.handler.UpStreamHandler;
import com.paxos.remoting.api.RemotingServer;
import com.paxos.remoting.netty.DefaultRemotingServer;

public class DefaultExChrangeServer implements ExchangeServer {

	private UpStreamHandler upStreamHandler;
	
	private RemotingServer remotingServer;

	public DefaultExChrangeServer(UpStreamHandler upStreamHandler) {
		this.upStreamHandler = upStreamHandler;
	}

	@Override
	public void start(String ip, Integer port) {
		remotingServer = new DefaultRemotingServer(upStreamHandler);
		remotingServer.start(ip, port);
	}

	@Override
	public void stop() {
		remotingServer.stop();
	}

}
