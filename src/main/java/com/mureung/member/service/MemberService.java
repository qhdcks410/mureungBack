package com.mureung.member.service;


import com.mureung.config.jwt.JwtToken;
import com.mureung.config.jwt.JwtTokenProvider;
import com.mureung.config.redis.RefreshTokenService;
import com.mureung.member.dto.Member;
import com.mureung.member.mapper.MemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;

@Service
public class MemberService{

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private AuthenticationManagerBuilder authenticationManagerBuilder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RefreshTokenService refreshTokenService;


    public List<Member> getMemberList() {
        return memberMapper.selectMemberList();
    }

    public Member getMember(@RequestParam HashMap<String,Object> param) {
        return memberMapper.selectMember((String)param.get("id"));
    }

    public JwtToken login(HashMap<String,Object> param) throws Exception {
        // 1. username + password 를 기반으로 Authentication 객체 생성
        // 이때 authentication 은 인증 여부를 확인하는 authenticated 값이 false
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(param.get("id"), param.get("pw"));

        // 2. 실제 검증. authenticate() 메서드를 통해 요청된 Member 에 대한 검증 진행
        // authenticate 메서드가 실행될 때 CustomUserDetailsService 에서 만든 loadUserByUsername 메서드 실행
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);
        // 4. 레디스에 저장하기
        refreshTokenService.saveRefreshToken(jwtToken.getRefreshToken(),(String)param.get("id"));

        return jwtToken;
    }

    public JwtToken refresh(HashMap<String,Object> param,String refreshToken) throws Exception {

        Member member = this.getMember(param);

        String  id = (String)param.get("id");

        // 1. username + password 를 기반으로 Authentication 객체 생성
        // 이때 authentication 은 인증 여부를 확인하는 authenticated 값이 false
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(member.getId(), member.getPw());

        // 2. 실제 검증. authenticate() 메서드를 통해 요청된 Member 에 대한 검증 진행
        // authenticate 메서드가 실행될 때 CustomUserDetailsService 에서 만든 loadUserByUsername 메서드 실행
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);

        // 4. 레디스에 삭제하기
        refreshTokenService.removeRefreshToken(id);

        // 4. 레디스에 저장하기
        refreshTokenService.saveRefreshToken(jwtToken.getRefreshToken(),id);

        return jwtToken;
    }


    public void logOut(HashMap<String, String> param) {
        refreshTokenService.removeRefreshToken((String)param.get("id"));
    }


}