package com.paxos.remoting.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.paxos.remoting.api.RemotingCommand;
import com.paxos.remoting.api.RemotingServer;
import com.paxos.remoting.io.codec.RemotingCommandDecoder;
import com.paxos.remoting.io.codec.RemotingCommandEncoder;


/**
 * IO通信服务器
 * @author sanfeng
 *
 */
public class IORemotingServer implements RemotingServer{  
     
	// 判断是否关闭服务器标志  
	private volatile boolean shutdown = false;  
    
	@Override
	public void start(String ip, int port) {  
		
		ServerSocket server = null;  
		try {  
			// 最多接受50个socket客户端连接
			server = new ServerSocket(port, 50, InetAddress.getByName(ip));  
		} catch (IOException e) {  
			e.printStackTrace();  
			System.exit(1);  
		}  
        
		//循环等待请求  
		while(!shutdown) {  
			Socket socket = null;    
            
			try {  
				socket = server.accept();  
				new Thread(new Worker(socket)).start();
				
			} catch (IOException e) {  
				e.printStackTrace();  
			}  
		}  
	}  
    
    
	@Override
	public void stop() {
		shutdown = true;
	}
	
	
	public class Worker implements Runnable {
		
		private Socket socket;
		
		Worker(Socket socket) {
			this.socket = socket;
		}
		
		@Override
		public void run() {
			
			InputStream input = null;
			OutputStream output = null;
			
			try {
				
				input = socket.getInputStream();
				output = socket.getOutputStream();  
	            
				
				// 如果没有停止服务器, 一直处理业务
				if(!shutdown) {
					// 解码获取command对象
					RemotingCommand reqCommand = RemotingCommandDecoder.decode(input);
					
					// 转交给业务处理
					// TODO
		            
					// 创建一个Response对象来输出内容  
					byte[] bytes = RemotingCommandEncoder.encode(null); 
					
					output.write(bytes);
					output.flush();
				}
				
				
			} catch (IOException e) {
				e.printStackTrace();
				
			} finally {  
				
				// 关闭输入流
				if(input != null) {
					try {
						input.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				// 关闭输出流
				if(output != null) {
					try {
						output.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
				//关闭socket  
				if(socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}  	
				}
			}
            
		}
	}
	
	
	public static void main(String[] args) {  
		IORemotingServer server = new IORemotingServer();  
		server.start("127.0.0.1", 8888);;  
	}
}
