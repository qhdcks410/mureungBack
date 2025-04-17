package com.mureung.customer.service;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.mureung.common.mapper.FileMapper;
import com.mureung.common.service.FileService;
import com.mureung.customer.dto.Customer;
import com.mureung.customer.mapper.CustomerMapper;
import com.mureung.member.dto.Member;

import io.jsonwebtoken.io.IOException;

@Service
public class CustomerService{

    @Autowired
    private	CustomerMapper customerMapper;

    @Autowired
    private	FileService fileService;

    public List<Customer> getCustomerList(Customer param) {
        return customerMapper.selectCustomerList(param);
    }

    public Member getMember(HashMap<String,Object> param) {
        return customerMapper.selectCustomer((String)param.get("cusNo"));
    }

    @Transactional
    public void insertCustomer(List<MultipartFile> files,Customer param) throws Exception  {
//    	Customer cus = customerMapper.selectCheckPhone(param) ;
//    	if(customerMapper.selectCheckPhone(param) == null) {
//        	customerMapper.insertCustomer(param);
//    	}else {
//    		param.setCusNo(cus.getCusNo());
//    	}
//
//    	if(!param.getCusNo().equals("")) {
//    		param.setCusNo(param.getCusNo());
//    		customerMapper.insertOrder(param);
//    	}

    	//파일이 있을떄 업로드
    	if(!files.isEmpty()) {
    		for (MultipartFile file : files) {
    			fileService.insertFile(file,param);
    		}
    	}
    }

    public void updateCompYn(Customer param) {
    	customerMapper.updateCompYn(param);
    }

    public void deleteOrader(String[] params) {
    	for(String order : params) {
    		customerMapper.deleteOrader(order);
    	}

    }

    public void modifyOrader(List<MultipartFile> files,Customer param) throws Exception{
		//customerMapper.updateOrader(param);

    	//파일이 있을떄 업로드
    	if(!files.isEmpty()) {
    		for (MultipartFile file : files) {
    			fileService.updateFile(file,param);
    		}
    	}
    }









}