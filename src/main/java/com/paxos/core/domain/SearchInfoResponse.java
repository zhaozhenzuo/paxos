package com.paxos.core.domain;

import java.io.Serializable;

public class SearchInfoResponse implements Serializable {

	private static final long serialVersionUID = 1L;

	String data;

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}
}
