package com.mureung.customer.dto;

import com.mureung.common.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Customer extends BaseDto {

	private String cusNo;
	private String cusNm;
	private String cusPhone;
	private String cusChnnel;
	private String orderNo;
	private String prodNm;
	private String conn;
	private String conDate;
	private String ordDate;
	private String regDate;
	private String regNm;
	private String updDate;
	private String updNm;
	private String compYn;
	private String payMd;
	private String payNt;
	private String ordAmt;
    private String ordOtherAmt;
	private String ordTime;

}
