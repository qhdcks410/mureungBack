package com.mureung.api.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OrderRequestDto {
    private String customerName;
    private String phoneNumber;
    private String orderInfo;
    private String pickupDate;
    private String pickupTime;
}
