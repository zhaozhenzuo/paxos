package com.paxos.remoting.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.Charset;

import com.paxos.core.constants.CodeInfo;
import com.paxos.remoting.api.RemotingCommand;

/**
 * 远程通信指令编码器<br/>
 * 
 * @author zhaozhenzuo
 *
 */
public class RemotingCommandEncoder extends MessageToByteEncoder<RemotingCommand> {

	@Override
	protected void encode(ChannelHandlerContext ctx, RemotingCommand msg, ByteBuf out) throws Exception {
		if (null == msg) {
			throw new Exception("msg is null");
		}

		String body = msg.getBody();
		byte[] bodyBytes = body.getBytes(Charset.forName("utf-8"));

		// 版本号
		out.writeByte(CodeInfo.VERSION);

		// 远程通信类型,请求或响应
		out.writeByte(msg.getCommandType());

		// 消息体长度
		out.writeInt(bodyBytes.length);

		// 消息体
		out.writeBytes(bodyBytes);
	}

}
