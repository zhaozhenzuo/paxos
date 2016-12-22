package com.paxos.config;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.paxos.core.domain.ElectionInfo;
import com.paxos.core.domain.PaxosMember;
import com.paxos.core.store.PaxosStoreInf;
import com.paxos.datastore.DataStoreInf;
import com.paxos.util.BizSerialAndDeSerialUtil;

@Component
public class ElectionInfoLoader {

	private static final Logger logger = Logger.getLogger(ElectionInfoLoader.class);;

	@Autowired
	private DataStoreInf dataStore;

	@Autowired
	private PaxosStoreInf paxosStore;

	public void loadElectionInfo() throws Exception {
		logger.info(">begin loader electionInfo from store");

		byte[] resBytes = dataStore.read();
		if (resBytes == null) {
			logger.info(">not found electionInfo from store");
			return;
		}

		ElectionInfo electionInfoSaved = BizSerialAndDeSerialUtil.byteToObjectByJson(resBytes, ElectionInfo.class);
		logger.info(">found electionInfo from store,res,electionInfo[" + JSON.toJSONString(electionInfoSaved) + "]");
		if (electionInfoSaved == null) {
			return;
		}

		/**
		 * 這里只保存
		 */
		PaxosMember currentMember = paxosStore.getCurrentPaxosMember();

		
		Long savedRealNum = electionInfoSaved.getRealNum();
		Object savedRealValue = electionInfoSaved.getRealValue();

		ElectionInfo oldElectionInfo = currentMember.getElectionInfo();

		String text = new String(resBytes);

		JSONObject jsonObject = JSON.parseObject(text);
		Long savedElectionRound = jsonObject.getLong("electionRound");
		
		if (savedElectionRound != null) {
			oldElectionInfo.setElectionRoundByValue(savedElectionRound);
		}

		if (savedRealNum != null) {
			oldElectionInfo.setRealNum(savedRealNum);
		}

		if (savedRealValue != null) {
			oldElectionInfo.setRealValue(savedRealValue);
		}
		return;
	}
}
