package com.mureung.customer.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.mureung.customer.dto.Customer;
import com.mureung.member.dto.Member;

@Repository
@Mapper
public interface CustomerMapper {
	List<Customer> selectCustomerList();
	Member selectCustomer(String cusNo);
	int insertCustomer(Customer customer);
	int insertOrder(Customer customer);
}
