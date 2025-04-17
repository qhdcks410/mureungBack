package com.mureung.common.controller;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mureung.common.dto.FileDto;
import com.mureung.common.service.FileService;
import com.mureung.customer.dto.Customer;
import com.mureung.customer.service.CustomerService;

@RestController
@RequestMapping("/file")
public class FileController {

	@Autowired
	private FileService fileService;

	@PostMapping("/selectFileList")
	public List<FileDto> selectImageList(@RequestParam  HashMap<String,Object> param){
	    return fileService.selectFileList(param);
	}


}
