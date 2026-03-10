package com.mureung.common.service;


import com.mureung.common.dto.FileDto;
import com.mureung.common.mapper.FileMapper;
import io.jsonwebtoken.io.IOException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Log4j2
@Service
public class FileService{
	// 1. 설정값 (본인의 환경에 맞게 수정)
	private static final String NAS_URL = "https://qhdcks410.tw2.quickconnect.to/webapi";
	private static final String ACCOUNT = "qhdcks410";
	private static final String PASSWORD = "BongChan88!";
	private static final String uploadDir = "/home/web/mureung/imageUpload";

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

	private void nasFileUploadServie(Path uploadPath) throws Exception{
//        HttpClient client = HttpClient.newHttpClient();
//
//		// 2. 로그인 (sid 획득)
//		String authUrl = NAS_URL + "/auth.cgi?api=SYNO.API.Auth&version=3&method=login&account="
//				+ ACCOUNT + "&passwd=" + PASSWORD + "&session=FileStation&format=sid";
//
//		HttpRequest authRequest = HttpRequest.newBuilder(URI.create(authUrl)).GET().build();
//		HttpResponse<String> authResponse = client.send(authRequest, HttpResponse.BodyHandlers.ofString());
//
//		// JSON 응답에서 sid 추출 (간단한 파싱)
//		String sid = authResponse.body().split("\"sid\":\"")[1].split("\"")[0];
//
//		// 3. 업로드 준비
//		String boundary = "JavaBoundary" + System.currentTimeMillis();
//		Path fileToUpload = uploadPath; // 실제 이미지 경로
//		String nasPath = uploadDir; // 업로드할 NAS 폴더 경로
//
//		// Multipart 바디 구성
//		String beforeFile = "--" + boundary + "\r\n" +
//				"Content-Disposition: form-data; name=\"api\"\r\n\r\nSYNO.FileStation.Upload\r\n" +
//				"--" + boundary + "\r\n" +
//				"Content-Disposition: form-data; name=\"version\"\r\n\r\n2\r\n" +
//				"--" + boundary + "\r\n" +
//				"Content-Disposition: form-data; name=\"method\"\r\n\r\nupload\r\n" +
//				"--" + boundary + "\r\n" +
//				"Content-Disposition: form-data; name=\"path\"\r\n\r\n" + nasPath + "\r\n" +
//				"--" + boundary + "\r\n" +
//				"Content-Disposition: form-data; name=\"_sid\"\r\n\r\n" + sid + "\r\n" +
//				"--" + boundary + "\r\n" +
//				"Content-Disposition: form-data; name=\"file\"; filename=\"" + fileToUpload.getFileName() + "\"\r\n" +
//				"Content-Type: image/jpeg\r\n\r\n";
//
//		String afterFile = "\r\n--" + boundary + "--\r\n";
//
//		// 4. 업로드 요청 전송
//		HttpRequest uploadRequest = HttpRequest.newBuilder(URI.create(NAS_URL + "/entry.cgi"))
//				.header("Content-Type", "multipart/form-data; boundary=" + boundary)
//				.POST(HttpRequest.BodyPublishers.concat(
//						HttpRequest.BodyPublishers.ofString(beforeFile),
//						HttpRequest.BodyPublishers.ofFile(fileToUpload),
//						HttpRequest.BodyPublishers.ofString(afterFile)
//				))
//				.build();
//
//		HttpResponse<String> uploadResponse = client.send(uploadRequest, HttpResponse.BodyHandlers.ofString());
//
//		// 5. 결과 출력
//		System.out.println("결과: " + uploadResponse.body());

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
			  Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING); //덮어쓰기
			  //this.nasFileUploadServie(destinationPath);
	          file.transferTo(destinationPath);

		      //fileMapper.insert(uniqueFileName);
		      log.info("파일 저장 성공: {} -> {}", originalFileName, destinationPath);
		  } catch (IOException e) {
		      e.printStackTrace();
		  }

		return filedto;
    }

}