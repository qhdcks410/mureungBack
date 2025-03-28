package com.mureung.member.service;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mureung.member.dto.Member;
import com.mureung.member.mapper.MemberMapper;

@Service
public class MemberService{

    @Autowired
    private MemberMapper memberMapper;
    
    public List<Member> getMemberList() {
        return memberMapper.selectMemberList();
    }
}