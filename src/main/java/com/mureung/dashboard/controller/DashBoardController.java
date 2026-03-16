package com.mureung.dashboard.controller;

import com.mureung.customer.dto.Customer;
import com.mureung.dashboard.dto.DashBoard;
import com.mureung.dashboard.service.DashBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashBoard")
public class DashBoardController {

	@Autowired
	private DashBoardService dashBoardService;

	@PostMapping("/getDashBoardList")
	public List<DashBoard> getDashBoardList(){
	    return dashBoardService.getDashBoardList();
	}

    @PostMapping("/getDashBoardGraphList")
    public List<DashBoard> getDashBoardGraphList(){
        return dashBoardService.getDashBoardGraphList();
    }


}
