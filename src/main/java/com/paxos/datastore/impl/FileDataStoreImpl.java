package com.paxos.datastore.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.paxos.config.PaxosConfig;
import com.paxos.core.constants.CodeInfo;
import com.paxos.core.domain.ElectionInfo;
import com.paxos.datastore.DataStoreInf;
import com.paxos.util.BizSerialAndDeSerialUtil;

/**
 * 文件储存持久化实现
 * 
 * @author shenxiu
 *
 */
@Repository
public class FileDataStoreImpl implements DataStoreInf {

	private static final Logger logger = Logger.getLogger(FileDataStoreImpl.class);

	@Autowired
	private PaxosConfig paxosConfig;

	private boolean createFilePathIfNotExist(String filePath) {
		if (StringUtils.isEmpty(filePath)) {
			return false;
		}

		File file = new File(filePath);
		boolean resFlag = true;
		if (!file.exists()) {
			resFlag = file.mkdirs();
			if (!resFlag) {
				logger.error(">cannot create filePath in writeToStore,filePath:" + filePath);
				return false;
			}
		}
		return resFlag;
	}

	private boolean createFileIfNotExist(String filePath, String fileName) {
		if (StringUtils.isEmpty(fileName)) {
			return false;
		}

		String fullPath = this.getFullName(filePath, fileName);
		File file = new File(fullPath);
		boolean resFlag = true;
		if (!file.exists()) {
			try {
				resFlag = file.createNewFile();
				if (!resFlag) {
					logger.error(">cannot create fileName in writeToStore,fileName:" + fileName);
				}
			} catch (IOException e) {
				logger.error(">create file err,fileName:" + fileName, e);
				resFlag = false;
			}

		}
		return resFlag;
	}

	@Override
	public boolean writeToStore(byte[] data) {
		String dataStorePath = paxosConfig.getDataStorePath();
		String fileName = paxosConfig.getDataStoreFileName();
		
		String logStr = "dataStorePath[" + dataStorePath + "],fileName[" + fileName + "]";

		logger.info(">" + logStr);

		if (StringUtils.isEmpty(dataStorePath)) {
			throw new IllegalArgumentException("dataStorePath cannot be null");
		}

		/**
		 * 目录处理
		 */
		boolean createFilePathRes = this.createFilePathIfNotExist(dataStorePath);
		if (!createFilePathRes) {
			logger.error(">filePath not found," + logStr);
			return false;
		}

		/**
		 * 文件处理
		 */
		boolean createFileRes = this.createFileIfNotExist(dataStorePath, fileName);
		if (!createFileRes) {
			logger.error(">file not found," + logStr);
			return false;
		}

		String fullPath = this.getFullName(dataStorePath, fileName);
		File file = new File(fullPath);
		FileOutputStream fout = null;
		try {
			fout = new FileOutputStream(file);
			fout.write(data);
			return true;
		} catch (FileNotFoundException e) {
			logger.error(">not found filePath when writeToStore," + logStr, e);
			return false;
		} catch (IOException e) {
			logger.error(">not found filePath when IOException," + logStr, e);
			return false;
		} finally {
			if (fout != null) {
				try {
					fout.close();
				} catch (IOException e) {
					logger.error(">close file err");
				}
			}
		}
	}

	private String getFullName(String path, String fileName) {
		String fileNameRes = fileName + "_" + paxosConfig.getIp() + "_" + paxosConfig.getPort();
		return path + File.separator + fileNameRes;
	}

	public static void main(String[] args) {
		ElectionInfo electionInfo = new ElectionInfo();
		electionInfo.setMaxAcceptFirstPhaseNum(200L);
		electionInfo.setElectionRoundByValue(2);

		byte[] res = BizSerialAndDeSerialUtil.objectToBytesByByJson(electionInfo);

		FileDataStoreImpl fileDataStoreImpl = new FileDataStoreImpl();

		PaxosConfig paxosConfig = new PaxosConfig();
		paxosConfig.setDataStorePath("d:\\paxos\\store");
		paxosConfig.setDataStoreFileName("data.json");

		fileDataStoreImpl.paxosConfig = paxosConfig;

		boolean writeRes = fileDataStoreImpl.writeToStore(res);
		System.out.println(writeRes);

		byte[] resFromFile = fileDataStoreImpl.read();
		if (resFromFile != null) {
			ElectionInfo electionInfoRes = BizSerialAndDeSerialUtil.byteToObjectByJson(resFromFile, ElectionInfo.class);
			System.out.println(electionInfoRes);
		}

	}

	@Override
	public boolean writeToStore(String data) {
		throw new UnsupportedOperationException("not support");
	}

	@Override
	public byte[] read() {
		String dataStorePath = paxosConfig.getDataStorePath();
		String fileName = paxosConfig.getDataStoreFileName();
		String logStr = "dataStorePath[" + dataStorePath + "],fileName[" + fileName + "]";
		logger.info(">" + logStr);

		if (StringUtils.isEmpty(dataStorePath)) {
			throw new IllegalArgumentException("dataStorePath cannot be null");
		}

		String fullPath = this.getFullName(dataStorePath, fileName);

		/**
		 * 读取数据
		 */
		BufferedReader breader = null;

		StringBuilder buf = new StringBuilder(48);
		String temp = null;

		try {
			breader = new BufferedReader(new FileReader(fullPath));
			while ((temp = breader.readLine()) != null) {
				buf.append(temp);
			}
		} catch (FileNotFoundException e) {
			logger.error(">not found file when read,fullPath[" + fullPath + "]");
		} catch (IOException e) {
			logger.error(">not found file when read,fullPath[" + fullPath + "]", e);
		} finally {
			if (breader != null) {
				try {
					breader.close();
				} catch (IOException e) {
					logger.error(">close breader err");
				}
			}
		}

		try {
			byte[] res = buf.toString().getBytes(CodeInfo.UTF_8);
			return res;
		} catch (UnsupportedEncodingException e) {
			logger.error(">UnsupportedEncodingException,fullPath[" + fullPath + "]", e);
			return null;
		}
	}
}
