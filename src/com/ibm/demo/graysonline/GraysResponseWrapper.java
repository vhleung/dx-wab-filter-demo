package com.ibm.demo.graysonline;

import java.io.IOException;

import javax.servlet.ServletOutputStream;

import com.ibm.wps.vwat.servlet.ReverseProxyResponse;

public class GraysResponseWrapper extends ReverseProxyResponse {

	private ReverseProxyResponse proxyResponse;
	private GraysOutputStreamWrapper sosw;

	public GraysResponseWrapper(ReverseProxyResponse proxyResponse, boolean isContentZip) {
		super(proxyResponse, proxyResponse.getConnection());
		this.proxyResponse = proxyResponse;
		sosw = new GraysOutputStreamWrapper();
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return sosw;
	}
}
