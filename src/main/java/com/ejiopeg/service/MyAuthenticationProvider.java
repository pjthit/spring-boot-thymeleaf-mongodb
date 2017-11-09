package com.ejiopeg.service;

import java.io.IOException;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.ejiopeg.model.Service;
import com.ejiopeg.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;

@Component
public class MyAuthenticationProvider implements AuthenticationProvider
																		//, UserDetailsService  
																		{
	
	private final Logger logger = LoggerFactory.getLogger(MyAuthenticationProvider.class);

	private static MessageDigestPasswordEncoder passwordEncoder = new ShaPasswordEncoder();
	
	@Value("${sitewhere.domain}")
	private String siteWhereDomain;
	
	@Value("${sitewhere.admin}")
	private String siteWhereAdmin;
	
	@Value("${sitewhere.password}")
	private String siteWherePassword;
	
	/*@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		User user = new User();
		user.setUsername(username);
		user.setSitewhereUsername(this.loginUsername);
		user.setSitewherePassword(this.loginPassword);
		return user;
	}*/

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		// TODO Auto-generated method stub
		String username = authentication.getName();
		String password = authentication.getCredentials().toString();
		String encoderPwd = passwordEncoder.encodePassword(password, null);
		//System.out.println("Response content: " + EntityUtils.toString(entity));
		JSONObject jo = null;
		try {
			String api = "/sitewhere/api/users/" + username;
			String url = "http://" + siteWhereDomain + api;
			String auth = "Basic " + Base64.getEncoder().encodeToString((siteWhereAdmin+":"+siteWherePassword).getBytes());
			HttpClient client = HttpClients.createDefault();
			HttpGet request = new HttpGet(url);
			request.setHeader("Authorization", auth);
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			jo = JSONObject.parseObject(EntityUtils.toString(entity));
		} catch (Exception e) {
			logger.error(e.toString());
		}
		if (jo == null) {
			throw new BadCredentialsException("External system authentication failed");
		}
		
		String stored_hashpwd = jo.getString("hashedPassword");
		if (encoderPwd.equals(stored_hashpwd)) {
			User user = new User();
			user.setUsername(username);
			user.setPassword(password);
			return new UsernamePasswordAuthenticationToken
		              (user, password, Collections.emptyList());
		} else {
            throw new BadCredentialsException("External system authentication failed");
        }
		
	}

	@Override
	public boolean supports(Class<?> authentication) {
		// TODO Auto-generated method stub
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

}
