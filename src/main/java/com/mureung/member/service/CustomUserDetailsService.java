package com.mureung.member.service;

import java.util.List;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.mureung.config.jwt.JwtTokenProvider;
import com.mureung.member.dto.Member;
import com.mureung.member.mapper.MemberMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final JwtTokenProvider jwtTokenProvider;

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    	UserDetails detail = null;
    	try {
        	Member memeber = memberMapper.selectMember(username);
        	detail = this.createUserDetails(memeber);
        	new UsernameNotFoundException("해당하는 회원을 찾을 수 없습니다.");
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
	 
        return detail;
    }

    // 해당하는 User 의 데이터가 존재한다면 UserDetails 객체로 만들어서 return
    private UserDetails createUserDetails(Member member) {
        return User.builder()
                .username(member.getId())
                .password(passwordEncoder.encode(member.getPw()))
                .roles(member.getRoles())
                .build();
    }

}
