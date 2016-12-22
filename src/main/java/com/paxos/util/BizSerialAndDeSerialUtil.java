package com.paxos.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.paxos.core.constants.CodeInfo;
import com.paxos.core.domain.ElectionRequest;
import com.paxos.core.domain.ElectionResultRequest;

public class BizSerialAndDeSerialUtil {

	private static final Logger logger = Logger.getLogger(BizSerialAndDeSerialUtil.class);

	public static byte[] objectToBytesByByJson(Object object) {
		return JSON.toJSONString(object).getBytes(Charset.forName(CodeInfo.UTF_8));
	}

	public static <T> T byteToObjectByJson(byte[] res, Class<T> clazz) {
		try {
			String resStr = new String(res, CodeInfo.UTF_8);
			return JSON.parseObject(resStr, clazz);
		} catch (UnsupportedEncodingException e) {
			logger.error(">UnsupportedEncodingException");
		}
		return null;
	}

	public static ElectionRequest parseElectionRequest(String requestData) {
		if (StringUtils.isEmpty(requestData)) {
			logger.error(">parseElectionRequest requestData is null," + requestData);
			return null;
		}

		try {
			return JSON.parseObject(requestData, ElectionRequest.class);
		} catch (Exception e) {
			logger.error(">parseElectionRequest err," + requestData, e);
			return null;
		}
	}

	public static ElectionResultRequest parseElectionResultRequest(String requestData) {
		if (StringUtils.isEmpty(requestData)) {
			logger.error(">parseElectionResultRequest requestData is null," + requestData);
			return null;
		}

		try {
			return JSON.parseObject(requestData, ElectionResultRequest.class);
		} catch (Exception e) {
			logger.error(">parseElectionResultRequest err," + requestData, e);
			return null;
		}
	}

}
