package com.ejiopeg.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ejiopeg.model.Customer;
import com.ejiopeg.model.Service;
import com.ejiopeg.model.User;
import com.ejiopeg.rabbitmq.RabbitmqMessageSender;
import com.ejiopeg.repository.CustomerRepository;
import com.ejiopeg.util.SitewhereUtil;

@RestController
@RequestMapping("/api/v1")
public class CustomerController {
	
	private final Logger logger = LoggerFactory.getLogger(CustomerController.class);

	@Autowired
	private CustomerRepository repository;
	
	@Autowired
	private RabbitmqMessageSender mqSender;
	
	@Autowired
	private SitewhereUtil sitewhereUtil;
	
	@RequestMapping("/customer")
	public void init() {
		
		repository.deleteAll();

		// save a couple of customers
		repository.save(new Customer("Alice", "Smith"));
		repository.save(new Customer("Bob", "Smith"));

		// fetch all customers
		System.out.println("Customers found with findAll():");
		System.out.println("-------------------------------");
		for (Customer customer : repository.findAll()) {
			System.out.println(customer);
		}
		System.out.println();

		// fetch an individual customer
		System.out.println("Customer found with findByFirstName('Alice'):");
		System.out.println("--------------------------------");
		System.out.println(repository.findByFirstName("Alice"));

		System.out.println("Customers found with findByLastName('Smith'):");
		System.out.println("--------------------------------");
		for (Customer customer : repository.findByLastName("Smith")) {
			System.out.println(customer);
		}
	}
	
	@RequestMapping(value = "/query")
	public List<Customer> getAllUser(){
		List<Customer> userList = repository.findAll();
		return userList;
		
	}
	
	@RequestMapping(value = "/add/{firstName}/{lastName}")
	public Customer addUser(@PathVariable String firstName,
	        @PathVariable String lastName) {
		Customer user = new Customer(firstName, lastName);
		repository.save(user);
		return user;
		
	}
	
	@RequestMapping(value = "/delete/{firstName}/{lastName}")
	public void deleteUser(@PathVariable String firstName,
	        @PathVariable String lastName) {
		repository.delete(new Customer(firstName, lastName));
	}
	
	@RequestMapping(value = "/sitewhere/tenant", method = RequestMethod.POST)
	public ResponseEntity<JSONArray> siteWhereTenant() {
		JSONArray result = null;
		try {
			//Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			//String username = String.valueOf(principal);
			User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			String username = user.getUsername();
			Map<String, String> params = new HashMap<String, String>();
			params.put("type", "GET");
			params.put("api", "/sitewhere/api/users/"+username+"/tenants");
			String jo = sitewhereUtil.send2Sitewhere(params);
			/*if(jo.startsWith("[")) {
				jo = jo.substring(1, jo.length()-1);
			}*/
			result = JSON.parseArray(jo);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new ResponseEntity<JSONArray>(result, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/sitewhere", method = RequestMethod.POST)
	public ResponseEntity<JSONArray> siteWhereSiteDevice(HttpServletRequest request, 
			HttpServletResponse response) {
		//Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		User user = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		//String username = user.getUsername();
		JSONArray result = null;
		String type = request.getParameter("type");
		String tenantToken = request.getParameter("tenantToken");
		String siteToken = request.getParameter("siteToken");
		String auth = "Basic " + Base64.getEncoder().encodeToString((user.getUsername()+":"+user.getPassword()).getBytes());
		try {
			
			Map<String, String> params = new HashMap<String, String>();
			params.put("type", "GET");
			params.put("api", "/sitewhere/api/" + type + "/");
			params.put("auth", auth);
			if (type.equalsIgnoreCase("sites")) {
				params.put("tenantToken", tenantToken);
			}else if (type.equals("devices")) {
				params.put("tenantToken", tenantToken);
				params.put("siteToken", siteToken);
			}
			String jo = sitewhereUtil.send2Sitewhere(params);
			if(jo.startsWith("[") || jo.startsWith("{")) {
				result = JSONObject.parseObject(jo).getJSONArray("results");
			}else {
				return new ResponseEntity<JSONArray>(result, HttpStatus.NOT_FOUND);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return new ResponseEntity<JSONArray>(result, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/rabbitmq/{content}")
	public void rabbitmqSender(@PathVariable String content) {
		mqSender.sendMessage(content);
	}
}
