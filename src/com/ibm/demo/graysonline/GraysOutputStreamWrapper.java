package com.ibm.demo.graysonline;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;

public class GraysOutputStreamWrapper extends ServletOutputStream {

	ByteArrayOutputStream bos = null;

	public GraysOutputStreamWrapper() {
		bos = new ByteArrayOutputStream();
	}

	@Override
	public void write(int b) throws IOException {
		bos.write(b);
	}

	public byte[] getModifiedBytes() {
		return bos.toByteArray();
	}
}
