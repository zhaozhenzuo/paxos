package com.paxos.core.service;

import com.paxos.core.domain.BaseElectionResponse;
import com.paxos.core.domain.ElectionRequest;
import com.paxos.core.domain.ElectionResultRequest;

/**
 * 接收者处理选举接口
 * 
 * @author shenxiu
 *
 */
public interface ElectionServiceForAcceptor {

	/**
	 * 接收者处理第一阶段请求提议
	 * 
	 * @param electionRequest
	 * @return
	 */
	public BaseElectionResponse processElectionRequestFirstPhase(ElectionRequest electionRequest);

	/**
	 * 接收者处理第二阶段请求提议
	 * 
	 * @param electionRequest
	 * @return
	 */
	public BaseElectionResponse processElectionRequestSecondPhase(ElectionRequest electionRequest);

	/**
	 * 接受者接受到选举结果的处理
	 * 
	 * @param request
	 * @return
	 */
	public BaseElectionResponse processElectionResultRequest(ElectionResultRequest request);

}
