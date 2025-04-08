package com.mureung.customer.service;


import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.mureung.customer.dto.Customer;
import com.mureung.customer.mapper.CustomerMapper;
import com.mureung.member.dto.Member;

@Service
public class CustomerService{

    @Autowired
    private	CustomerMapper customerMapper;

    public List<Customer> getCustomerList() {
        return customerMapper.selectCustomerList();
    }

    public Member getMember(HashMap<String,Object> param) {
        return customerMapper.selectCustomer((String)param.get("cusNo"));
    }

    public void insertCustomer(Customer param) {
    	if(customerMapper.insertCustomer(param) > 0) {
    		customerMapper.insertOrder(param);
    	}
    }



}