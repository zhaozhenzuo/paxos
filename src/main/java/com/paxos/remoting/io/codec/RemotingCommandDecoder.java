package com.paxos.remoting.io.codec;

import java.io.IOException;
import java.io.InputStream;

import com.paxos.remoting.api.RemotingCommand;
import com.paxos.util.ByteUtil;

/**
 * IO解码器
 * @author sanfeng
 *
 */
public class RemotingCommandDecoder {
	
	public static final String charset = "utf-8";

	public static RemotingCommand decode(InputStream in) {
		
 		try {
 			
 			// 根据InputStream进行解码
 			byte[] versions = new byte[1];
 			byte[] commandTypes = new byte[1];
 			byte[] lengths = new byte[4];
			in.read(versions);
			in.read(commandTypes);
			in.read(lengths);
			
			byte version = versions[0];
			byte commandType = commandTypes[0];
			int length = ByteUtil.byte2Int(lengths);
			
			byte[] bodys = new byte[length];
			in.read(bodys);
			String body = new String(bodys, charset);
			
			// 组装解码结果
			RemotingCommand command = new RemotingCommand();
			command.setVersion(version);
			command.setCommandType(commandType);
			command.setLength(length);
			command.setBody(body);
			
			return command;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
}
