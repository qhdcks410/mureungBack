package com.mureung.dashboard.mapper;

import com.mureung.customer.dto.Customer;
import com.mureung.dashboard.dto.DashBoard;
import com.mureung.member.dto.Member;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface DashBoardMapper {
    List<DashBoard> selectDashBoardList();

    List<DashBoard> selectDashBoardGraphList();
}
