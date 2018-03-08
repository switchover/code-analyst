package com.samsungsds.analyst.code.sonar.server.servlets;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.samsungsds.analyst.code.util.IOAndFileUtils;

@SuppressWarnings("serial")
public class QualityProfilesServlet extends HttpServlet {
	private static final Logger LOGGER = LogManager.getLogger(QualityProfilesServlet.class);
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String url = request.getRequestURI();
		String queryString = request.getQueryString() == null ? "" : "?" + request.getQueryString();
		
		LOGGER.debug("Requested URL : {}{}", url, queryString);
		
		// Declare response encoding and types
		response.setContentType("application/octet-stream");

		// Declare response status code
		response.setStatus(HttpServletResponse.SC_OK);

		String resourceName = null;
		
		if (url.equals("/api/qualityprofiles/search.protobuf")) {
			resourceName = "/statics/search.protobuf";
		} else if (url.equals("/api/rules/search.protobuf")) {
			// URL ex : /api/rules/search.protobuf?f=repo,name,severity,lang,internalKey,templateKey,params,actives,createdAt&activation=true&qprofile=AWHam0EHkasiS3G9MIkQ&p=1&ps=500

			if (!queryString.contains("&p=1&")) {
				LOGGER.error("'p' parameter error : {}", queryString);
				throw new IllegalArgumentException("Request URL parameter error ('p' isn't 1");
			}

			if (queryString.contains("qprofile=AWEgcpHElIthtMf8fR6x")) {
				resourceName = "/statics/AWEgcpHElIthtMf8fR6x.protobuf";
			} else if (queryString.contains("qprofile=AWEmUh5aAmOaUaoeB5Tf")) {
				resourceName = "/statics/AWEmUh5aAmOaUaoeB5Tf.protobuf";
			} else if (queryString.contains("qprofile=AWEgcpZ4lIthtMf8fSH3")) {
				resourceName = "/statics/AWEgcpZ4lIthtMf8fSH3.protobuf";
			} else if (queryString.contains("qprofile=AWEgcpWvlIthtMf8fSG4")) {
				resourceName = "/statics/AWEgcpWvlIthtMf8fSG4.protobuf";
			} else if (queryString.contains("qprofile=AWEgcpjplIthtMf8fSNd")) {
				resourceName = "/statics/AWEgcpjplIthtMf8fSNd.protobuf";
			} else if (queryString.contains("qprofile=AWEgcpnxlIthtMf8fSPc")) {
				resourceName = "/statics/AWEgcpnxlIthtMf8fSPc.protobuf";
			} else if (queryString.contains("qprofile=AWEgcpyRlIthtMf8fSSN")) {
				resourceName = "/statics/AWEgcpyRlIthtMf8fSSN.protobuf";
			} else if (queryString.contains("qprofile=AWEgcp25lIthtMf8fSTR")) {
				resourceName = "/statics/AWEgcp25lIthtMf8fSTR.protobuf";
			} else if (queryString.contains("qprofile=AWGIMrVIyNk_vPtwVsb_")) {
				resourceName = "/statics/AWGIMrVIyNk_vPtwVsb_.protobuf";
			} else if (queryString.contains("qprofile=AWGIMrq3yNk_vPtwVsea")) {
				resourceName = "/statics/AWGIMrq3yNk_vPtwVsea.protobuf";
			} else if (queryString.contains("qprofile=AWGIMr_nyNk_vPtwVsg1")) {
				resourceName = "/statics/AWGIMr_nyNk_vPtwVsg1.protobuf";
			} else if (queryString.contains("qprofile=AWHam0EHkasiS3G9MIkQ")) {
				resourceName = "/statics/AWHam0EHkasiS3G9MIkQ.protobuf";
			}
		} else if (url.equals("/api/rules/list.protobuf")) {
			resourceName = "/statics/list.protobuf";
		} else if (url.equals("/batch/project.protobuf")) {
			resourceName = "/statics/project.protobuf";
		}
		
		if (resourceName == null) {
			throw new IllegalArgumentException("URL or queryString error...");
		}
		
		// Write back response
		try (OutputStream outStream = response.getOutputStream()) {
			IOAndFileUtils.write(outStream, resourceName);
		}
	}
}
