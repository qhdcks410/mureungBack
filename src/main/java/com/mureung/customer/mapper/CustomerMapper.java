package com.mureung.customer.mapper;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.mureung.customer.dto.Customer;
import com.mureung.member.dto.Member;

@Repository
@Mapper
public interface CustomerMapper {
	List<Customer> selectCustomerList(Customer param);
	Member selectCustomer(String cusNo);
	void insertCustomer(Customer customer);
	int insertOrder(Customer customer);
	int updateCompYn(Customer customer);
	Customer selectCheckPhone(Customer param);

}
