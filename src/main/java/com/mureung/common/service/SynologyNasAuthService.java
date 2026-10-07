package com.mureung.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.jboss.aerogear.security.otp.Totp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Log4j2
@Service
public class SynologyNasAuthService {

    @Autowired
    private ObjectMapper objectMapper;

    final private RestTemplate restTemplate = new RestTemplate();

    @Value("${synology.nas.url}") private String nasUrl;
    @Value("${synology.nas.account}") private String account;
    @Value("${synology.nas.password}") private String password;
    @Value("${synology.nas.otp-secret}") private String otpSecret;

    /**
     * 실시간 OTP 번호를 생성하여 Synology NAS 로그인 API를 호출합니다.
     *
     * @return NAS에서 반환한 JSON 결과 문자열 (성공 시 sid 포함)
     */
    public String loginAndGetSid() {
        try {
            // 1. 실시간 6자리 OTP 번호 동적 생성
            String currentOtpCode = new Totp(otpSecret).now();
            log.info("생성된 실시간 OTP 번호: {}", currentOtpCode);

            // 2. URL 빌드 및 HTTP GET 요청 연쇄 호출(Chaining) 후 결과 반환
            String jsonResponse = restTemplate.getForObject(
                    UriComponentsBuilder.fromHttpUrl(nasUrl)
                            .path("/webapi/entry.cgi")
                            .queryParam("api", "SYNO.API.Auth")
                            .queryParam("version", "3")
                            .queryParam("method", "login")
                            .queryParam("account", account)
                            .queryParam("passwd", password)
                            .queryParam("session", "FileStation")
                            .queryParam("format", "sid")
                            .queryParam("otp_code", currentOtpCode)
                            .toUriString(),
                    String.class
            );

            log.info("NAS 로그인 API 응답 JSON: {}", jsonResponse);

            return this.extractSid(jsonResponse);

        } catch (Exception e) {
            log.error("Synology API 로그인 요청 중 오류 발생", e);
            throw new RuntimeException("NAS 로그인 실패: " + e.getMessage(), e);
        }
    }

    /**
     * Synology 응답 JSON 문자열에서 sid를 파싱합니다.
     */
    private String extractSid(String jsonResponse) throws Exception {
        if (jsonResponse == null || jsonResponse.isBlank()) {
            throw new IllegalStateException("NAS 응답 값이 비어 있습니다.");
        }

        JsonNode rootNode = objectMapper.readTree(jsonResponse);

        // API 성공 여부 확인 ("success": true)
        boolean isSuccess = rootNode.path("success").asBoolean(false);
        if (!isSuccess) {
            String errorCode = rootNode.path("error").path("code").asText("알 수 없음");
            throw new RuntimeException("Synology 로그인 실패 (Error Code: " + errorCode + ")");
        }

        // data 노드 아래의 sid 추출
        JsonNode sidNode = rootNode.path("data").path("sid");
        if (sidNode.isMissingNode() || sidNode.asText().isBlank()) {
            throw new IllegalStateException("응답은 성공했으나 JSON 내부에 sid 값이 존재하지 않습니다.");
        }

        String sid = sidNode.asText();
        log.info("추출 성공한 SID: {}", sid);
        return sid;
    }

}
