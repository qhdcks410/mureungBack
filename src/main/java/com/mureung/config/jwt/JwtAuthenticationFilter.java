package com.mureung.config.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {
    private final JwtTokenProvider jwtTokenProvider;


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            String token = resolveToken((HttpServletRequest) request);

            // 1. 토큰이 있는 경우 검증
            if (token != null) {
                if (jwtTokenProvider.validateToken(token)) {
                    Authentication auth = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
            // 2. 다음 필터로 진행 (토큰이 없어도 인증이 필요 없는 API일 수 있으므로 진행)
            chain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            // 토큰 만료: 401 에러와 전용 코드를 보냄 -> 프론트가 재발급 로직 실행
            sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, "ERR_TOKEN_EXPIRED");
        } catch (JwtException | IllegalArgumentException e) {
            // 그 외 유효하지 않은 토큰: 403 에러 -> 프론트가 세션 종료
            sendErrorResponse(httpResponse, HttpServletResponse.SC_FORBIDDEN, "ERR_INVALID_TOKEN");
        }
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String code) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(String.format("{\"status\": %d, \"code\": \"%s\"}", status, code));
    }

    // Request Header에서 토큰 정보 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
