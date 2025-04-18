package com.mureung.common.controller;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mureung.common.dto.FileDto;
import com.mureung.common.service.FileService;

import io.jsonwebtoken.io.IOException;

@RestController
@RequestMapping("/file")
public class FileController {

	@Autowired
	private FileService fileService;

	@PostMapping("/selectFileList")
	public List<FileDto> selectFileList(@RequestParam  HashMap<String,Object> param){
	    return fileService.selectFileList(param);
	}

	@PostMapping("/selectRefFileList")
	public List<FileDto> selectRefFileList(@RequestBody  HashMap<String,Object> param){
	    return fileService.selectRefFileList(param);
	}

	@GetMapping("/image/view/{fileId}")
	public ResponseEntity<byte[]> display(@PathVariable("fileId")String fileId)throws Exception {
		HashMap<String,Object> param = new HashMap<String,Object>();
		param.put("fileId", fileId);
		FileDto fileDto = fileService.selectFile(param);

		File file = new File(fileDto.getStoredPath());

		//저장된 이미지파일의 이진데이터 형식을 구함
		byte[] result=null;//1. data
		ResponseEntity<byte[]> entity=null;

		try {
	    	result = FileCopyUtils.copyToByteArray(file);

			//2. header
			HttpHeaders header = new HttpHeaders();
			header.add("Content-type",Files.probeContentType(file.toPath())); //파일의 컨텐츠타입을 직접 구해서 header에 저장

			//3. 응답본문
			entity = new ResponseEntity<>(result,header,HttpStatus.OK);//데이터, 헤더, 상태값
		} catch (IOException e) {
			e.printStackTrace();
		}

		return entity;
	}

}
