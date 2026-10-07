package com.mureung.common.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mureung.common.dto.FileDto;
import com.mureung.common.mapper.FileMapper;
import io.jsonwebtoken.io.IOException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Log4j2
@Service
public class FileService{

    @Autowired
    private ObjectMapper objectMapper;

	// 1. 설정값 (본인의 환경에 맞게 수정)
    @Value("${synology.nas.url}") private String nasUrl;
    @Value("${synology.nas.upload-dir}") private String uploadDir;

    @Autowired
    private	FileMapper fileMapper;

    @Autowired
    private	SynologyNasAuthService synologyNasAuthService;

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
        // 1. 나스 로그인 및 권한체크 SID 획득
        String sid = synologyNasAuthService.loginAndGetSid();

        // [방어 코드] 업로드할 로컬 파일이 실제로 존재치 않으면 시놀로지에 요청하지 않고 즉시 예외 처리
        if (uploadPath == null || !Files.exists(uploadPath)) {
            throw new java.io.FileNotFoundException("업로드할 로컬 파일이 존재하지 않습니다: " + uploadPath);
        }

        // 2. 바운더리 생성
        String boundary = "JavaBoundary" + System.currentTimeMillis();

        // 3. ⚠️ 중요: 시놀로지 NAS 내부의 목적지 폴더 경로 설정
        String currentDate = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String targetNasPath = uploadDir + currentDate;

        // 4. UriComponentsBuilder를 사용하여 안전하게 업로드 API 엔드포인트 URL 생성
        URI uploadUri = UriComponentsBuilder.fromHttpUrl(nasUrl)
                .path("/webapi/entry.cgi")
                .queryParam("_sid", sid)
                .build()
                .toUri();

        // 5. Multipart Form-Data 바디 문자열 조립
        String beforeFile = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"api\"\r\n\r\nSYNO.FileStation.Upload\r\n" +
                "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"version\"\r\n\r\n2\r\n" +
                "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"method\"\r\n\r\nupload\r\n" +
                "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"path\"\r\n\r\n" + targetNasPath + "\r\n" +
                "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"create_parents\"\r\n\r\ntrue\r\n" + // 상위 폴더 자동 생성 옵션 추가 (안정성 향상)
                "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"file\"; filename=\"" + uploadPath.getFileName().toString() + "\"\r\n" +
                "Content-Type: image/jpeg\r\n\r\n";

        String afterFile = "\r\n--" + boundary + "--\r\n";

        // 6. 파서 무반응(no bytes) 에러 방지를 위해 HttpClient 빌더 최적화
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)     // 👈 클라이언트 레벨에서 HTTP/1.1 강제 고정
                .connectTimeout(Duration.ofSeconds(10))   // 연결 타임아웃 10초 부여
                .build();

        // 7. 업로드 API 호출
        HttpRequest uploadRequest = HttpRequest.newBuilder(uploadUri)
                .timeout(Duration.ofMinutes(2))           // 업로드 대기 타임아웃 2분 부여
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.concat(
                        HttpRequest.BodyPublishers.ofString(beforeFile),
                        HttpRequest.BodyPublishers.ofFile(uploadPath), // 주입받은 로컬 Path 파일 바디 매핑
                        HttpRequest.BodyPublishers.ofString(afterFile)
                ))
                .build();

        // 8. 요청 전송 및 응답 확인
        HttpResponse<String> uploadResponse = client.send(uploadRequest, HttpResponse.BodyHandlers.ofString());
        log.info("시놀로지 파일 업로드 응답 결과: {}", uploadResponse.body());
        parseUploadResponse(uploadResponse.body());

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

            String originalFullNm = file.getOriginalFilename();
            String baseName = (originalFullNm != null && originalFullNm.contains(".")) ? originalFullNm.substring(0, originalFullNm.lastIndexOf(".")) : originalFullNm;
            String extension = (originalFullNm != null && originalFullNm.contains(".")) ? originalFullNm.substring(originalFullNm.lastIndexOf(".")) : "";

            String originalFileName = baseName + "_" + formattedDateTime + extension;
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

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            }
            // 파일 저장
            this.nasFileUploadServie(destinationPath);
            log.info("파일 저장 성공: {} -> {}", originalFileName, destinationPath);
		  } catch (IOException e) {
		      e.printStackTrace();
		  }

		return filedto;
    }

    /**
     * 시놀로지 업로드 결과 JSON을 파싱하여 응답 코드를 검증합니다.
     */
    private void parseUploadResponse(String jsonResponse) throws Exception {
        if (jsonResponse == null || jsonResponse.isBlank()) {
            throw new IllegalStateException("시놀로지 서버로부터 응답 값이 유입되지 않았습니다.");
        }

        // JSON 트리 읽기
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        boolean isSuccess = rootNode.path("success").asBoolean(false);

        // 실패 상태인 경우 오류 코드 매핑 처리
        if (!isSuccess) {
            int errorCode = rootNode.path("error").path("code").asInt(-1);
            String errorMessage = getNasErrorMessage(errorCode);

            log.error("❌ 시놀로지 파일 업로드 실패: [Code {}] {}", errorCode, errorMessage);
            throw new RuntimeException("NAS 업로드 실패: " + errorMessage + " (Error Code: " + errorCode + ")");
        }

        log.info("🎉 시놀로지 파일 업로드가 성공적으로 완료되었습니다.");
    }

    /**
     * 시놀로지 가이드 문서 기반 에러 코드 한글 매핑 함수
     */
    private String getNasErrorMessage(int errorCode) {
        switch (errorCode) {
            case 100: return "알 수 없는 시스템 오류가 발생했습니다.";
            case 101: return "잘못된 파라미터 요청(Bad Request)입니다.";
            case 102: return "요청한 API가 시놀로지에 존재하지 않습니다.";
            case 103: return "요청한 Method가 존재하지 않습니다.";
            case 104: return "지원하지 않는 API 버전입니다.";
            case 105: return "해당 계정에 접근 권한(Privilege)이 부족합니다.";
            case 106: return "세션이 타임아웃 되었습니다.";
            case 107: return "중복 로그인으로 인해 기존 세션이 끊어졌습니다.";
            case 119: return "유효하지 않은 세션(Invalid Session)이거나 sid가 유효하지 않습니다. 로그인을 재시도하세요.";
            case 150: return "로그인한 IP 주소와 현재 업로드를 요청한 IP 주소가 일치하지 않습니다.";
            case 400: return "공유 폴더를 찾을 수 없거나 가상 경로가 잘못되었습니다.";
            case 401: return "지정된 자격 증명 세션이 올바르지 않습니다.";
            case 408: return "파일을 업로드할 수 있는 디렉토리 권한이 없습니다.";
            default: return "정의되지 않은 시놀로지 내부 오류가 발생했습니다.";
        }
    }

}