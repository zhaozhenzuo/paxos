package com.paxos.remoting.io.codec;

import java.io.UnsupportedEncodingException;

import com.paxos.remoting.api.RemotingCommand;
import com.paxos.util.ByteUtil;

/**
 * 编码器
 * @author sanfeng
 *
 */
public class RemotingCommandEncoder {

	public static final String charset = "utf-8";
	
	public static byte[] encode(RemotingCommand command) {
	
		// outBytes总长度
		int length = 1 + 1 + 4 + command.getLength();
		byte[] outBytes = new byte[length];
		
		// 组装version  1个字节
		outBytes[0] = command.getVersion();
		// 组装commandType  1个字节
		outBytes[1] = command.getCommandType();
		// 组装length  4个字节
		byte[] lengths = ByteUtil.int2Byte(command.getLength());
		System.arraycopy(lengths, 0, outBytes, 2, 4);
		
		// 组装body
		try {
			byte[] bodys = command.getBody().getBytes(charset);
			System.arraycopy(bodys, 0, outBytes, 6, command.getLength());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return outBytes;
	}
	
}
