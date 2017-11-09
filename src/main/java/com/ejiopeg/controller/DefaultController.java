package com.ejiopeg.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.ejiopeg.util.SitewhereUtil;


@Controller
public class DefaultController {

	private final Logger logger = LoggerFactory.getLogger(DefaultController.class);
	
	@GetMapping("/")
	public String index() {
		return "login";
	}

	@GetMapping("portal")
	public String portal() {
		return "portal";
	}

	@GetMapping("home")
	public String home() {
		return "home";
	}
	
	@GetMapping("device")
	public String device() {
		return "device";
	}

	@GetMapping("login")
	public String login() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		logger.debug(username + "login success!");
		return "login";
	}

	@GetMapping("logout")
	public String logout(HttpServletRequest request) {
		Principal principal = request.getUserPrincipal();
		logger.debug(principal.getName() + "logout success!");
		return "login";
	}

}
