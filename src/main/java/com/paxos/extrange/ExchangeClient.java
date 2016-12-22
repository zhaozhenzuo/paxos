package com.paxos.extrange;

import com.paxos.remoting.api.RemotingCommand;

/**
 * 一个client实例代表一条与server连接的socket通道
 * 
 * @author zhaozhenzuo
 *
 */
public interface ExchangeClient {

	public Object sendSync(RemotingCommand req) throws Exception;
	
	public ResponseFuture sendAsyncSync(RemotingCommand req) throws Exception;

	public boolean connect(String ip, Integer port);

	public void stop();
	
	public long getLastLiveTime();
	
	public void setLastLiveTime(long time);
	
	public boolean isStoped();

}
