package com.paxos.extrange;

/**
 * 结果future接口
 * 
 * @author zhaozhenzuo
 *
 */
public interface ResponseFuture {

	/**
	 * 获取结果
	 * 
	 * @return
	 * @throws Exception
	 */
	public Object get() throws Exception;

	/**
	 * 指定时间内，获取结果，超时抛异常
	 * 
	 * @param timeout
	 *            单位毫秒
	 * @return
	 * @throws Exception
	 */
	public Object get(long timeout) throws Exception;

}
