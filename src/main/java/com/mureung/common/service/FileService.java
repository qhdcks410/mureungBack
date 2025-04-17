package com.mureung.common.service;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mureung.common.dto.FileDto;
import com.mureung.common.mapper.FileMapper;
import com.mureung.customer.dto.Customer;
import com.mureung.customer.mapper.CustomerMapper;
import com.mureung.member.dto.Member;

import io.jsonwebtoken.io.IOException;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class FileService{

    @Autowired
    private	FileMapper fileMapper;

    public List<FileDto> selectFileList(HashMap<String,Object> param) {
        return fileMapper.selectFileList(param);
    }

    String uploadDir = "C:\\\\dev\\\\upload";

    public void insertFile(MultipartFile file,Customer customer) throws Exception{
    	this.transferFile(file);
    }

    public void updateFile(MultipartFile file,Customer customer) throws Exception{
    	this.transferFile(file);
    }

    private void transferFile(MultipartFile file) throws Exception{

        // 1. 파일 저장 경로 설정 (폴더가 없으면 생성)
        Path uploadPath = Paths.get(uploadDir);
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("업로드 디렉토리 생성: {}", uploadPath);
            }
        } catch (IOException e) {
        	e.printStackTrace();
        }

        // 3. 파일 저장 처리
        String originalFileName = file.getOriginalFilename();
        // 보안: 파일 이름 충돌 방지 및 경로 조작 방지를 위해 고유한 이름 생성 권장
        String savedFileName = UUID.randomUUID().toString() + "_" + originalFileName;
        Path destinationPath = uploadPath.resolve(Paths.get(savedFileName)).normalize();


        // 혹시 모를 경로 조작 방지 (예: ../../filename)
        if (!destinationPath.getParent().equals(uploadPath.normalize())) {
        	log.error("잘못된 파일 경로입니다: {}", savedFileName);
        	throw new Exception();
        }


		  try {
		      // 파일 저장
			  //Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING); //덮어쓰기
		      file.transferTo(destinationPath);
		      //fileMapper.insert(uniqueFileName);
		      //log.info("파일 저장 성공: {} -> {}", originalFileName, destinationPath);
		  } catch (IOException e) {
		      e.printStackTrace();
		  }
    }

}