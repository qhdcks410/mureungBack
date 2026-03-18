package com.mureung.common.scheduler;

import com.mureung.common.service.EmailService;
import com.mureung.customer.dto.Customer;
import com.mureung.customer.mapper.CustomerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PickupEmailScheduler {

    private final EmailService emailService;
    private final CustomerMapper customerMapper; // 또는 CustomerRepository (DB 조회용)

    // cron 표현식: 초 분 시 일 월 요일
    @Scheduled(cron = "0 0 7 * * *")
    public void sendDailyPickupEmail() {
        log.info("매일 오전 7시 픽업 리스트 메일 발송 시작");

        try {
            // 1. 오늘 날짜의 픽업 리스트 DB에서 조회 (본인의 로직에 맞게 수정)
            List<Customer> todayOrders = customerMapper.selectBatchOrderList();

            if (todayOrders != null && !todayOrders.isEmpty()) {
                // 2. 관리자 또는 방앗간 공용 메일로 발송
                emailService.sendPickupListEmail("qhdcks410@naver.com", todayOrders);
                log.info("총 {}건의 주문 정보를 발송했습니다.", todayOrders.size());
            } else {
                log.info("오늘 예정된 픽업 주문이 없습니다.");
            }
        } catch (Exception e) {
            log.error("픽업 메일 발송 중 오류 발생: {}", e.getMessage());
        }
    }
}
