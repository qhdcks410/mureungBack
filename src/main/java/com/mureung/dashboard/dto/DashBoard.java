package com.mureung.dashboard.dto;

import com.mureung.common.dto.BaseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashBoard extends BaseDto {

    private Integer todOrdCnt;
    private Integer pickCnt;
    private Integer todAmt;

    private String dt;
    private Integer totalAmt;
    private Integer monAmt;
    private Integer lastAmt;
    private Integer growthRate;



}
