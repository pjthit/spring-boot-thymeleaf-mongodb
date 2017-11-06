package com.ejiopeg.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Base64;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ejiopeg.model.Customer;
import com.ejiopeg.model.Service;
import com.ejiopeg.model.User;
import com.ejiopeg.repository.CustomerRepository;

@RestController
@RequestMapping("/api/v1")
public class CustomerController {
	
	private final Logger logger = LoggerFactory.getLogger(CustomerController.class);

	@Autowired
	private CustomerRepository repository;
	
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
	
	@RequestMapping(value = "/{domain}", method = RequestMethod.POST)
	public void siteWhere(@PathVariable String domain, HttpServletRequest httpRequest) {
		try {
			String ipport = "";
			String auth = "";
			User userDetails = (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			if (userDetails.getServices() != null && userDetails.getServices().size() != 0) {
				for (Service service : userDetails.getServices()) {
					if (service.getServiceName().equals(domain)) {
						ipport = service.getServiceUrl();
						auth = service.getAuth();
					}
				}
			}
			String api = httpRequest.getParameter("api");
			if (ipport == null || api == null || ipport == "" || api == "") {
				ResponseEntity.badRequest();
				return;
			}
			//String url = "http://" + siteWhere + "/sitewhere/api/assets/categories";
			String url = "http://" + ipport + api;
			auth = "Basic " + Base64.getEncoder().encodeToString((auth).getBytes());
			HttpClient client = HttpClients.createDefault();
			HttpGet request = new HttpGet(url);
			request.setHeader("Authorization", auth);
			request.setHeader("X-SiteWhere-Tenant", "sitewhere1234567890");
			HttpResponse response = client.execute(request);
			HttpEntity entity = response.getEntity();
			//System.out.println("Response content: " + EntityUtils.toString(entity));
			JSONObject jo = new JSONObject(EntityUtils.toString(entity));
			//JSONArray joResult = jo.getJSONArray("results");
			ResponseEntity.ok(jo);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		}
	}
}
