package com.mureung.common.service;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mureung.common.dto.FileDto;
import com.mureung.common.mapper.FileMapper;

import io.jsonwebtoken.io.IOException;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class FileService{

	String uploadDir = "C:\\\\dev\\\\upload";

    @Autowired
    private	FileMapper fileMapper;

    public List<FileDto> selectFileList(HashMap<String,Object> param) {
        return fileMapper.selectFileList(param);
    }

    public List<FileDto> selectRefFileList(HashMap<String,Object> param) {
        return fileMapper.selectRefFileList(param);
    }

    public FileDto selectFile(HashMap<String,Object> param) {
        return fileMapper.selectFile(param);
    }

    public void insertFile(List<MultipartFile> files,String refNo) throws Exception{
    	if(files != null) {
        	for (MultipartFile file : files) {
    	    	FileDto fileDto = this.transferFile(file,refNo);
    	    	fileMapper.insertFile(fileDto);
        	}
    	}

    }

    public void updateFile(List<MultipartFile> files,String refNo) throws Exception{
    	fileMapper.deleteFile(refNo);
    	if(files != null) {
        	for (MultipartFile file : files) {
        		FileDto fileDto = this.transferFile(file,refNo);
        		fileMapper.insertFile(fileDto);
        	}
    	}
    }

    private FileDto transferFile(MultipartFile file,String refNo) throws Exception{

    	FileDto  filedto = new FileDto();
        // 1. 파일 저장 경로 설정 (폴더가 없으면 생성)
        // 2. 현재 날짜 및 시간 가져오기
        LocalDateTime now = LocalDateTime.now();

        Path uploadPath = Paths.get(uploadDir,now.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("업로드 디렉토리 생성: {}", uploadPath);
            }

            // 3. 날짜/시간 포맷 지정 (파일 이름에 적합하게, 밀리초까지 포함)
            String formattedDateTime = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

	        // 3. 파일 저장 처리
	        String originalFileName = file.getOriginalFilename() + "_" + formattedDateTime;
	        // 보안: 파일 이름 충돌 방지 및 경로 조작 방지를 위해 고유한 이름 생성 권장
	        String savedFileName = UUID.randomUUID().toString() + "_" + originalFileName;
	        Path destinationPath = uploadPath.resolve(Paths.get(savedFileName)).normalize();


	        //DB 저장할 DTO 넣기
	        filedto.setOrigNm(file.getOriginalFilename());
	        filedto.setServerNm(savedFileName);
	        filedto.setStoredPath(destinationPath.toString());
	        filedto.setFsize(file.getSize());
	        filedto.setMimeType(file.getContentType());
	        filedto.setRefNo(refNo);

	        // 혹시 모를 경로 조작 방지 (예: ../../filename)
	        if (!destinationPath.getParent().equals(uploadPath.normalize())) {
	        	log.error("잘못된 파일 경로입니다: {}", savedFileName);
	        	throw new Exception();
	        }
		      // 파일 저장
			  //Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING); //덮어쓰기
	          file.transferTo(destinationPath);

		      //fileMapper.insert(uniqueFileName);
		      log.info("파일 저장 성공: {} -> {}", originalFileName, destinationPath);
		  } catch (IOException e) {
		      e.printStackTrace();
		  }

		return filedto;
    }

}