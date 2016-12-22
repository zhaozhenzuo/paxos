package com.paxos.datastore;

/**
 * 数据存储接口，实现可以是文件储存，数据库储存等
 * 
 * @author shenxiu
 *
 */
public interface DataStoreInf {

	/**
	 * 将指定字节写入到存储
	 * 
	 * @param data
	 * @return
	 */
	public boolean writeToStore(byte[] data);

	/**
	 * 将指定字符串写入到存储
	 * 
	 * @param data
	 * @return
	 */
	public boolean writeToStore(String data);

	/**
	 * 读取写入的数据
	 * 
	 * @return
	 */
	public byte[] read();
}
