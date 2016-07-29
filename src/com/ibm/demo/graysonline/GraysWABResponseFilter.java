package com.ibm.demo.graysonline;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.ibm.wps.vwat.servlet.ReverseProxyResponse;
import com.ibm.wps.vwat.servlet.utils.IOSupport;

public class GraysWABResponseFilter implements Filter {

	private static final int BYTE_BUFFER_SIZE = 8192;

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {
		ReverseProxyResponse proxyResponse = (ReverseProxyResponse) response;
		String contentType = proxyResponse.getContentType();
		if (contentType != null) {
			boolean isContentZip = false;
			if (contentType.contains("text") || contentType.contains("javascript")) {
				Collection<String> headerNames = proxyResponse.getHeaderNames();
				for (String headerName : headerNames) {
					if (headerName != null) {
						if (headerName.trim().equalsIgnoreCase("Content-Encoding")) {
							if (proxyResponse.getHeader(headerName) != null) {
								if ((proxyResponse.getHeader(headerName).indexOf("gzip") != -1)) {
									isContentZip = true;
								}
							}
						}
					}
				}

				GraysResponseWrapper wrapper = new GraysResponseWrapper(proxyResponse, isContentZip);
				chain.doFilter(request, wrapper);
				try {
					processContent(proxyResponse,
							((GraysOutputStreamWrapper) (wrapper.getOutputStream())).getModifiedBytes(), isContentZip);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				/* Continue the chain */
				chain.doFilter(request, response);
			}

		} else {
			/* Continue the chain */
			chain.doFilter(request, response);
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub

	}

	public String getModifiedContent(String originalContent) {
		String stringToBeReplaced = "</head>";
		String substitue = "<style> .main-navigation, #pageTopHeaderBar, #pageHeaderBar, #pageHeader, #pageContentFooter { display: none; } #pageMainNavWidgets { display: none; } #pageContent { padding-top: 0; top: 0; } .search2 .sale-search-results .nine-dollar-sale { width: 29px !important; } #pageContent .search2 .item-search-results td a, #pageContent .search2 .sale-search-results td a, #pageContent .all-sales .sales td a { border: none !important; } </style> </head>";
		String modifiedContent = null;
		if (originalContent.contains(stringToBeReplaced)) {
			System.out.println("SampleRMFilter write replacing: " + stringToBeReplaced + " with " + substitue);
			modifiedContent = originalContent.replaceAll(stringToBeReplaced, substitue);
		} else {
			modifiedContent = originalContent;
		}

		return modifiedContent;
	}

	public void processContent(ServletResponse response, byte[] originalBytes, boolean isContentZip) throws Exception {
		String origString = null;
		String modifiedString = null;
		InputStream in = new ByteArrayInputStream(originalBytes);

		if (isContentZip) {
			in = new GZIPInputStream(in, BYTE_BUFFER_SIZE);
			originalBytes = IOSupport.readStream(in);
		}

		origString = new String(originalBytes, "UTF-8");
		modifiedString = getModifiedContent(origString);
		byte[] modifiedBytes = modifiedString.getBytes("UTF-8");
		int modContentLength = modifiedBytes.length;

		if (isContentZip) {
			ByteArrayOutputStream baos = null;
			baos = new ByteArrayOutputStream();
			GZIPOutputStream zipOut = new GZIPOutputStream(baos);

			if (modifiedBytes != null) {
				zipOut.write(modifiedBytes, 0, modContentLength);
			}

			zipOut.finish();
			zipOut.flush();
			modifiedBytes = baos.toByteArray();
		}

		response.setContentLength(modContentLength);
		response.getOutputStream().write(modifiedBytes);
	}

}
