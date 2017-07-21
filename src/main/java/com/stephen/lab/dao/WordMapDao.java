package com.stephen.lab.dao;

import com.stephen.lab.dto.WordMapDto;
import com.stephen.lab.model.WordMap;
import com.stephen.lab.util.BaseDao;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WordMapDao extends BaseDao<WordMap>{
    List<WordMapDto> selectDto(@Param("condition") WordMap condition);
}
