package com.mureung.api.controller;

import com.mureung.api.dto.OrderRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api")
public class OrderApiController {


    @PostMapping("/orders")
    public ResponseEntity<String> saveGoogleFormOrder(@RequestBody OrderRequestDto requestDto) {
        // 데이터 수신 확인용 로그
        System.out.println("구글 폼 데이터 수신: " + requestDto.toString());

        // 여기서 DB 저장(JPA Repository 호출 등)을 수행하세요.
        // orderService.save(requestDto);

        return ResponseEntity.ok("데이터 저장 성공");
    }
}
