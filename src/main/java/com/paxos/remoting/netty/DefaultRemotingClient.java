package com.paxos.remoting.netty;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.paxos.core.constants.CodeInfo;
import com.paxos.protocal.handler.UpStreamHandler;
import com.paxos.remoting.api.RemotingClient;
import com.paxos.remoting.netty.codec.RemotingCommandDecoder;
import com.paxos.remoting.netty.codec.RemotingCommandEncoder;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class DefaultRemotingClient implements RemotingClient {

	private static final Logger logger = Logger.getLogger(DefaultRemotingClient.class);

	private Bootstrap bootstrap;

	private NioEventLoopGroup workerGroup;

	private Channel channel;

	private UpStreamHandler upStreamHandler;

	public DefaultRemotingClient(UpStreamHandler upStreamHandler) {
		this.upStreamHandler = upStreamHandler;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	@Override
	public boolean connect(String ip, Integer port) {
		logger.debug(">begin connect to server,ip[" + ip + "],port[" + port + "]");
		bootstrap = new Bootstrap();
		workerGroup = new NioEventLoopGroup(1);
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.group(workerGroup);
		bootstrap.option(ChannelOption.TCP_NODELAY, true);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000);

		bootstrap.handler(new ChannelInitializer<Channel>() {
			protected void initChannel(Channel ch) throws Exception {
				ch.pipeline().addLast(new RemotingCommandEncoder());
				ch.pipeline().addLast(new RemotingCommandDecoder(CodeInfo.MAX_MSG_SIZE, 2, 4, 0, 0, false));
				ch.pipeline().addLast(new UpStreamHandler(upStreamHandler.getPaxosCoreComponent()));
			}
		});

		SocketAddress remoteAddress = new InetSocketAddress(ip, port);
		ChannelFuture channelFuture;
		try {
			channelFuture = bootstrap.connect(remoteAddress).sync();
			channel = channelFuture.channel();
			logger.debug(">end succ connect server,ip[" + ip + "],port[" + port + "]");
			return true;
		} catch (Exception e) {
			// logger.error(">err connect server,ip[" + ip + "],port[" + port + "]", e);
			// TODO,只是测试
			logger.debug(">err connect server,ip[" + ip + "],port[" + port + "]");
			return false;
		}
	}

	@Override
	public void stop() {
		logger.debug(">close channel[" + JSON.toJSONString(channel) + "]");

		if (channel == null) {
			logger.error(">not found channel when to close channel");
			return;
		}

		if (!channel.isActive()) {
			logger.error(">current channel is closed already,channel[" + JSON.toJSONString(channel) + "]");
			return;
		}

		ChannelFuture closeFuture = channel.close();
		if (closeFuture.isSuccess()) {
			logger.debug(">succ close channel[" + JSON.toJSONString(channel) + "]");
		}
	}

	@Override
	public void send(Object msg) {
		channel.writeAndFlush(msg);
	}

	@Override
	public Object sendSync(Object msg) {
		return null;
	}

	@Override
	public void sendAsyncSync(Object msg) {

	}

}
