package com.mureung.common.service;

import com.mureung.customer.dto.Customer;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private final String SITE_URL = "http://localhost:5173/login?redirect=/dashboard/default";

    public void sendPickupListEmail(String toEmail, List<Customer> orderList) {

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("[무릉방앗간] " + today + " 주문 및 픽업 정보 안내");

            StringBuilder rows = new StringBuilder();
            for (Customer order : orderList) {
                String prodNm = order.getProdNm();
                if (prodNm != null && prodNm.length() > 20) {
                    prodNm = prodNm.substring(0, 20) + "...";
                }

                rows.append("<tr style='border-bottom: 1px solid #eee; text-align: center;'>")
                        .append("<td style='padding: 12px 5px; font-size: 14px;'>").append(order.getCusNm()).append("</td>")
                        .append("<td style='padding: 12px 5px; font-size: 14px; text-align: left;' title='").append(order.getProdNm()).append("'>").append(prodNm).append("</td>")
                        .append("<td style='padding: 12px 5px; font-size: 14px;'>").append(order.getConDate()).append("</td>")
                        // 금액 오른쪽 정렬 및 우측 여백 추가
                        .append("<td style='padding: 12px 10px; font-size: 14px; text-align: right;'>").append(String.format("%,d원", order.getOrdAmt())).append("</td>")
                        // 잔금 오른쪽 정렬 및 우측 여백 추가
                        .append("<td style='padding: 12px 10px; font-size: 14px; text-align: right; color: #d32f2f; font-weight: bold;'>").append(String.format("%,d원", order.getOrdOtherAmt())).append("</td>")
                        .append("<td style='padding: 12px 5px; font-size: 14px;'>").append(this.formatAnyPhoneNumber(order.getCusPhone())).append("</td>")
                        .append("</tr>");
            }

            String htmlContent =
                    "<div style='font-family: sans-serif; max-width: 850px; border: 1px solid #ddd; padding: 25px; color: #333;'>" +
                            "  <h2 style='color: #5d4037; margin-top: 0;'>🌾 무릉방앗간 주문 정보 (" + today + ")</h2>" +
                            "  <p style='margin-bottom: 20px; font-size: 15px;'>안녕하세요. <strong>" + today + "</strong> 픽업 예정인 주문 리스트입니다.</p>" +
                            "  <table style='width: 100%; border-collapse: collapse; table-layout: fixed; min-width: 700px; border-top: 2px solid #5d4037;'>" +
                            "    <thead>" +
                            "      <tr style='background-color: #f8f9fa; border-bottom: 1px solid #5d4037;'>" +
                            "        <th style='width: 12%; padding: 12px 5px; font-size: 13px; '>성함</th>" +
                            "        <th style='width: 30%; padding: 12px 5px; font-size: 13px; '>주문내역</th>" +
                            "        <th style='width: 16%; padding: 12px 5px; font-size: 13px; '>픽업시간</th>" +
                            "        <th style='width: 14%; padding: 12px 10px; font-size: 13px;'>금액</th>" +
                            "        <th style='width: 14%; padding: 12px 10px; font-size: 13px;'>잔금</th>" +
                            "        <th style='width: 14%; padding: 12px 5px; font-size: 13px; '>연락처</th>" +
                            "      </tr>" +
                            "    </thead>" +
                            "    <tbody>" +
                            rows.toString() +
                            "    </tbody>" +
                            "  </table>" +
                            "  <div style='margin-top: 40px; text-align: center;'>" +
                            "    <a href='" + SITE_URL + "' style='background-color: #5d4037; color: #ffffff; padding: 14px 30px; text-decoration: none; border-radius: 6px; font-weight: bold; font-size: 16px; display: inline-block;'>" +
                            "      무릉방앗간 시스템 바로가기" +
                            "    </a>" +
                            "  </div>" +
                            "  <div style='margin-top: 40px; padding-top: 15px; border-top: 1px solid #eee; font-size: 12px; color: #888;'>" +
                            "    <p style='margin: 0;'>* 본 메일은 무릉방앗간 시스템에서 자동 발송되었습니다.</p>" +
                            "  </div>" +
                            "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String formatAnyPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) return "";
        String src = phone.replaceAll("[^0-9]", "");
        int len = src.length();
        if (len == 8) return src.replaceFirst("^(\\d{4})(\\d{4})$", "$1-$2");
        if (src.startsWith("02")) {
            if (len == 9) return src.replaceFirst("^(\\d{2})(\\d{3})(\\d{4})$", "$1-$2-$3");
            if (len == 10) return src.replaceFirst("^(\\d{2})(\\d{4})(\\d{4})$", "$1-$2-$3");
        } else {
            if (len == 10) return src.replaceFirst("^(\\d{3})(\\d{3})(\\d{4})$", "$1-$2-$3");
            if (len == 11) return src.replaceFirst("^(\\d{3})(\\d{4})(\\d{4})$", "$1-$2-$3");
        }
        return phone;
    }
}
