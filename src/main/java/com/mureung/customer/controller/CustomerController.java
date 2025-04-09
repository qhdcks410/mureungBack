package com.mureung.customer.controller;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mureung.customer.dto.Customer;
import com.mureung.customer.service.CustomerService;
import com.mureung.member.dto.Member;

@RestController
@RequestMapping("/customer")
public class CustomerController {

	@Autowired
	private CustomerService customerService;

	@PostMapping("/getCustomerList")
	public List<Customer> getCustomerList(@RequestBody Customer param){
	    return customerService.getCustomerList(param);
	}

	@PostMapping("/getCustomer")
	public Member selectMember(@RequestBody HashMap<String,Object> param){
	    return customerService.getMember(param);
	}

	@PostMapping("/insertCustomer")
	public void insertCustomer(@RequestBody Customer param){
	    customerService.insertCustomer(param);
	}

	@PostMapping("/updateCompYn")
	public void updateCompYn(@RequestBody Customer param){
	    customerService.updateCompYn(param);
	}

}
