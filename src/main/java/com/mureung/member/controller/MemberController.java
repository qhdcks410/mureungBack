package com.mureung.member.controller;

import com.mureung.config.jwt.JwtToken;
import com.mureung.member.dto.Member;
import com.mureung.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

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

	@PostMapping("/logOut")
	public void logOut(@RequestBody HashMap<String, String> param) throws Exception {
        memberService.logOut(param);
    }

	@PostMapping("/auth/refresh")
	public JwtToken refresh(@RequestBody HashMap<String, Object> param,@RequestHeader HashMap<String, Object> heder) throws Exception {
		return memberService.refresh(param,(String)heder.get("Authorization"));
	}


}
