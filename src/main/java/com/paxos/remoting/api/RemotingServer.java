package com.paxos.remoting.api;

public interface RemotingServer {
	
	public void start(String ip,int port);
	
	public void stop();

}
