package com.mureung.common.mapper;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.mureung.common.dto.FileDto;

@Repository
@Mapper
public interface FileMapper {
	List<FileDto> selectFileList(HashMap<String,Object> param);
}
