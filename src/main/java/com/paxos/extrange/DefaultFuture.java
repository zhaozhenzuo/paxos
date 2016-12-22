package com.paxos.extrange;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.fastjson.JSON;
import com.paxos.core.constants.CodeInfo;
import com.paxos.remoting.api.RemotingCommand;
import com.paxos.remoting.api.RemotingRequest;
import com.paxos.remoting.api.RemotingResponse;

public class DefaultFuture implements ResponseFuture {

	private RemotingResponse response;

	private Long id;

	private static final Map<Long, DefaultFuture> futureMap = new ConcurrentHashMap<Long, DefaultFuture>();

	private static final Map<Long, ExchangeClient> exchangeClientMap = new ConcurrentHashMap<Long, ExchangeClient>();

	private volatile boolean done;

	private Lock lock = new ReentrantLock();

	private Condition lockCondition = lock.newCondition();

	private static final int DEFAULT_TIME_OUT_MILLISECONDS = 6000;

	public DefaultFuture(RemotingCommand command, ExchangeClient exchangeClient) {
		done = false;
		if (command.getCommandType() != CodeInfo.COMMAND_TYPE_REQ) {
			throw new IllegalArgumentException("DefaultFuture,reqType must be COMMAND_TYPE_REQ");
		}

		String body = command.getBody();
		RemotingRequest remotingRequest = JSON.parseObject(body, RemotingRequest.class);
		if (remotingRequest == null || remotingRequest.getReqId() == null) {
			throw new IllegalArgumentException("reqId cannot be null");
		}
		id = remotingRequest.getReqId();
		futureMap.put(id, this);
		exchangeClientMap.put(id, exchangeClient);
	}

	private boolean isDone() {
		return this.response != null;
	}

	public Object get() throws Exception {
		lock.lock();
		long oldTime = System.currentTimeMillis();
		try {
			if (!done) {
				while (!this.isDone()) {
					/**
					 * 超时就退出
					 */
					if ((System.currentTimeMillis() - oldTime) > DEFAULT_TIME_OUT_MILLISECONDS) {
						break;
					}
					lockCondition.await(DEFAULT_TIME_OUT_MILLISECONDS, TimeUnit.MILLISECONDS);
				}

			}

			if (!done) {
				throw new TimeoutException("get timeout");
			}
		} finally {
			lock.unlock();
		}

		return response;
	}

	public Object get(long timeout) throws Exception {
		throw new UnsupportedOperationException("暂不支持暂时获取get操作");
	}

	public static void recieve(RemotingResponse response) {
		long reqId = response.getReqId();
		try {
			DefaultFuture defaultFuture = futureMap.remove(reqId);
			if (defaultFuture != null) {
				defaultFuture.doRecieve(response);
			}
		} finally {
			exchangeClientMap.remove(reqId);
		}
	}

	private void doRecieve(RemotingResponse res) {
		lock.lock();
		try {
			this.response = res;
			this.lockCondition.signalAll();
			this.done = true;
		} finally {
			lock.unlock();
		}
	}

	public static ExchangeClient getExchangeClientByReqId(long reqId) {
		return exchangeClientMap.get(reqId);

	}

}
