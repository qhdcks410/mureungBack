package com.mureung.member.controller;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mureung.config.jwt.JwtToken;
import com.mureung.member.dto.Member;
import com.mureung.member.service.MemberService;

@RestController
@RequestMapping("/member")
public class MemberController {
	
	@Autowired
	private MemberService memberService;
	
	@PostMapping("/getMemberList")
	public List<Member> getMemberList(){
	    return memberService.getMemberList();
	}
	
	@PostMapping("/getMember")
	public Member selectMember(@RequestBody HashMap<String,Object> param){
	    return memberService.getMember(param);
	}	
	
	@PostMapping("/login")
	public JwtToken login(@RequestBody HashMap<String,Object> param) throws Exception {
	    return memberService.login(param);
	}	
}
