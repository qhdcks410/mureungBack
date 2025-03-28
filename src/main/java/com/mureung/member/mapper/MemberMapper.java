package com.mureung.member.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.mureung.member.dto.Member;

@Repository
@Mapper
public interface MemberMapper {
	List<Member> selectMemberList();
}
