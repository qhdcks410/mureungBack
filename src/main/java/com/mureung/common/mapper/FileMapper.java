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
	List<FileDto> selectRefFileList(HashMap<String,Object> param);
	FileDto selectFile(HashMap<String,Object> param);
	int insertFile(FileDto param);
	int updateFile(FileDto param);
	int deleteFile(String refNo);
}
