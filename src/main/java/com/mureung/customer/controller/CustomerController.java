package com.mureung.customer.controller;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
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
	public void insertCustomer(@RequestParam(value="editorFiles", required = false) List<MultipartFile> files,@RequestParam("saveData") String saveData) throws Exception{
		ObjectMapper objectMapper = new ObjectMapper();
		Customer customer = objectMapper.readValue(saveData, Customer.class);
	    customerService.insertCustomer(files,customer);
	}

	@PostMapping("/updateCompYn")
	public void updateCompYn(@RequestBody Customer param){
	    customerService.updateCompYn(param);
	}

	@PostMapping("/deleteOrader")
	public void deleteOrader(@RequestBody String[] param){
	    customerService.deleteOrader(param);
	}

	@PostMapping("/modifyOrader")
	public void modifyOrader(@RequestParam(value="editorFiles", required = false) List<MultipartFile> files,@RequestParam("saveData") String saveData) throws Exception{
		ObjectMapper objectMapper = new ObjectMapper();
		Customer customer = objectMapper.readValue(saveData, Customer.class);
	    customerService.modifyOrader(files,customer);
	}

}
