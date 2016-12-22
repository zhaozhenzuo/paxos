package com.paxos.remoting.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.log4j.Logger;

import com.paxos.core.constants.CodeInfo;
import com.paxos.protocal.handler.UpStreamHandler;
import com.paxos.remoting.api.RemotingServer;
import com.paxos.remoting.netty.codec.RemotingCommandDecoder;
import com.paxos.remoting.netty.codec.RemotingCommandEncoder;

public class DefaultRemotingServer implements RemotingServer {

	private static final Logger logger = Logger.getLogger(DefaultRemotingServer.class);

	private UpStreamHandler upStreamHandler;

	private String ip;

	private int port;

	public DefaultRemotingServer(UpStreamHandler upStreamHandler) {
		this.upStreamHandler = upStreamHandler;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	@Override
	public void start(String ip, int port) {
		this.ip = ip;
		this.port = port;

		ServerBootstrap serverBootstrap = new ServerBootstrap();

		NioEventLoopGroup boss = new NioEventLoopGroup(1);
		NioEventLoopGroup worker = new NioEventLoopGroup(16);
		serverBootstrap.group(boss, worker);
		serverBootstrap.option(ChannelOption.SO_BACKLOG, 1000);
		serverBootstrap.channel(NioServerSocketChannel.class);

		/**
		 * 监听的ip地址及端口
		 */
		SocketAddress socketAddress = new InetSocketAddress(ip, port);
		serverBootstrap.localAddress(socketAddress);

		/**
		 * 设置解码器及业务处理器
		 */
		serverBootstrap.childHandler(new ChannelInitializer<Channel>() {
			protected void initChannel(Channel ch) throws Exception {
				ch.pipeline().addLast(new RemotingCommandEncoder());
				ch.pipeline().addLast(new RemotingCommandDecoder(CodeInfo.MAX_MSG_SIZE, 2, 4, 0, 0, false));
				ch.pipeline().addLast(new UpStreamHandler(upStreamHandler.getPaxosCoreComponent()));
			}
		});

		try {
			ChannelFuture channelFuture = serverBootstrap.bind().sync();
			logger.info(">start remoting server succFlag:" + channelFuture.isSuccess());

		} catch (InterruptedException e) {
			logger.error(">start remoting server err,ip[" + ip + "],port[" + port + "]", e);
		}

	}

	@Override
	public void stop() {

	}

}
