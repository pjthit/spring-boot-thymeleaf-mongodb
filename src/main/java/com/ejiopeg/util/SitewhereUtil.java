package com.ejiopeg.util;

import java.util.Base64;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SitewhereUtil {
	
	private final static Logger logger = LoggerFactory.getLogger(SitewhereUtil.class);

	@Value("${sitewhere.domain}")
	private String siteWhereDomain;
	
	@Value("${sitewhere.admin}")
	private String siteWhereAdmin;
	
	@Value("${sitewhere.password}")
	private String siteWherePassword;
	
	public String send2Sitewhere(Map<String, String> params) {
		
		String result = null;
		String type = params.get("type");
		String api = params.get("api");
		String tenantToken = params.get("tenantToken");
		String siteToken = params.get("siteToken");
		String auth = params.get("auth");
		String url = "http://" + siteWhereDomain + api;
		if (auth == null) {
			auth = "Basic " + Base64.getEncoder().encodeToString((siteWhereAdmin+":"+siteWherePassword).getBytes());
		}
		try {
			HttpClient client = HttpClients.createDefault();
			HttpRequestBase request = null;
			if (type.equalsIgnoreCase("GET")) {
				request = new HttpGet(url);
			}else if (type.equalsIgnoreCase("post")) {
				request = new HttpPost(url);
			}else {
				request = new HttpGet(url);
			}
			request.setHeader("Authorization", auth);
			if (tenantToken != null) {
				request.setHeader("X-SiteWhere-Tenant", tenantToken);
			}
			if (siteToken != null) {
				request.setHeader("site", siteToken);
			}
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			result = EntityUtils.toString(entity);
		} catch (Exception e) {
			logger.error(e.toString());
		}
		return result;
		
	}
}
