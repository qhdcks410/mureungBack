package com.mureung.dashboard.service;


import com.mureung.customer.dto.Customer;
import com.mureung.dashboard.dto.DashBoard;
import com.mureung.dashboard.mapper.DashBoardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashBoardService {

    @Autowired
    private DashBoardMapper dashBoardMapper;

    public List<DashBoard> getDashBoardList() {
        return dashBoardMapper.selectDashBoardList();
    }

    public List<DashBoard> getDashBoardGraphList() {
        return dashBoardMapper.selectDashBoardGraphList();
    }
}