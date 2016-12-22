package com.paxos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * 保存paxos结点配置信息
 * 
 * @author zhaozhenzuo
 *
 */
@Configuration
@PropertySource({ "classpath:config.properties" })
public class PaxosConfig {

	private static final String IP_KEY = "ip";

	private static final String PORT_KEY = "port";

	private static final String NAME_KEY = "name";

	private static final String NODES_KEY = "nodes";

	public static final String NODES_SPLIT = ",";

	public static final String IP_PORT_SPLIT_FOR_NODES = ":";

	/**
	 * 数据存储位置参数key
	 */
	private static final String DATA_STORE_PATH_KEY = "data.store.path";
	
	private static final String DATA_STORE_FILENAME_KEY = "data.store.filename";
	

	/**
	 * 本结点名称
	 */
	@Value("${" + NAME_KEY + "}")
	private String memberName;

	/**
	 * 本结点ip
	 */
	@Value("${" + IP_KEY + "}")
	private String ip;

	/**
	 * 本结点选举port
	 */
	@Value("${" + PORT_KEY + "}")
	private String port;

	/**
	 * 全部结点
	 */
	@Value("${" + NODES_KEY + "}")
	private String clusterNodes;

	/**
	 * 心跳端口=选举端口+1
	 */
	private String heartbeatPort;

	@Value("${" + DATA_STORE_PATH_KEY + "}")
	private String dataStorePath;
	
	@Value("${" + DATA_STORE_FILENAME_KEY + "}")
	private String dataStoreFileName;

	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getClusterNodes() {
		return clusterNodes;
	}

	public void setClusterNodes(String clusterNodes) {
		this.clusterNodes = clusterNodes;
	}

	public String getHeartbeatPort() {
		return heartbeatPort;
	}

	public void setHeartbeatPort(String heartbeatPort) {
		this.heartbeatPort = heartbeatPort;
	}

	public String getDataStorePath() {
		return dataStorePath;
	}

	public void setDataStorePath(String dataStorePath) {
		this.dataStorePath = dataStorePath;
	}

	public String getDataStoreFileName() {
		return dataStoreFileName;
	}

	public void setDataStoreFileName(String dataStoreFileName) {
		this.dataStoreFileName = dataStoreFileName;
	}

}
