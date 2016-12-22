package com.paxos.remoting.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import com.paxos.remoting.api.RemotingClient;
import com.paxos.remoting.api.RemotingCommand;
import com.paxos.remoting.io.codec.RemotingCommandDecoder;
import com.paxos.remoting.io.codec.RemotingCommandEncoder;

/**
 * 
 * @author sanfeng
 *
 */
public class IORemotingClient implements RemotingClient{
	
	private Socket socket;
	
	private OutputStream output;
	
	private InputStream input;

	
	@Override
	public boolean connect(String ip, Integer port) {
		
		try {
			this.socket = new Socket(ip, port);
			this.output = socket.getOutputStream();
			this.input = socket.getInputStream();
			return true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	
	@Override
	public void stop() {
		if(this.input != null) {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(this.output != null) {
			try {
				this.output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(this.socket != null) {
			try {
				this.socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	
	@Override
	public Object sendSync(Object msg) {
		
		if(RemotingCommand.class.isAssignableFrom(msg.getClass())) {
			
			try {
				
				// 发送请求消息
				byte[] bytes = RemotingCommandEncoder.encode((RemotingCommand)msg);
				output.write(bytes);
				output.flush();
				
				// 同步阻塞返回接收结果
				return RemotingCommandDecoder.decode(input);
				
			} catch (IOException e) {
				e.printStackTrace();
			} 
			
		}
		
		return null;
	}

	@Override
	public void sendAsyncSync(Object msg) {
	}


	@Override
	public void send(Object msg) {
	}

}
