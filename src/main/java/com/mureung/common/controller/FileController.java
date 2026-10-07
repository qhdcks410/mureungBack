package com.mureung.common.controller;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import com.mureung.common.service.SynologyNasAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/file")
public class FileController {

    @Value("${synology.nas.url}")
    private String nasUrl;

	@Autowired
	private FileService fileService;

    @Autowired
    private SynologyNasAuthService synologyNasAuthService;

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
        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("fileId", fileId);
        FileDto fileDto = fileService.selectFile(param);

        // 💡 1. 기존 로컬 File 객체 대신, DB에 저장된 NAS 파일 경로(StoredPath)를 사용합니다.
        String nasFilePath = fileDto.getStoredPath().replace("\\", "/");

        byte[] result = null;
        ResponseEntity<byte[]> entity = null;
        HttpHeaders header = new HttpHeaders();

        try {
            // 💡 2. 나스 로그인 및 SID 획득
            String sid = synologyNasAuthService.loginAndGetSid();
            RestTemplate restTemplate = new RestTemplate();

            // 💡 3. 시놀로지 이미지 다운로드(원본) URI 생성
            URI nasUri = UriComponentsBuilder.fromHttpUrl(nasUrl)
                    .path("/webapi/entry.cgi")
                    .queryParam("api", "SYNO.FileStation.Download")
                    .queryParam("version", "2")
                    .queryParam("method", "download")
                    .queryParam("path", nasFilePath)
                    .queryParam("mode", "open") // 💡 브라우저가 다운로드창을 띄우지 않고 화면에 바로 그리도록 설정
                    .queryParam("_sid", sid)
                    .build()
                    .toUri();

            // 💡 4. NAS로부터 이미지 이진 데이터(byte[]) 직접 획득
            result = restTemplate.getForObject(nasUri, byte[].class);

            // 💡 5. 헤더 설정 (MimeType 추출)
            // NAS 경로는 로컬 파일 객체가 아니므로, 파일명 스펙(확장자)을 기반으로 Content-Type을 추론합니다.
            String contentType = Files.probeContentType(Paths.get(nasFilePath));
            if (contentType == null) {
                contentType = "image/jpeg"; // 기본값 예외 처리
            }
            header.add("Content-type", contentType);

            // 6. 응답본문 구성
            entity = new ResponseEntity<>(result, header, HttpStatus.OK);

        } catch (Exception e) { // IOException을 포함한 전역 예외 처리
            e.printStackTrace();
            entity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return entity;
	}

}
