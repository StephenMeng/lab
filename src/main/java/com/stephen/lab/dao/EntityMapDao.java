package com.stephen.lab.dao;

import com.stephen.lab.dto.EntityMapDto;
import com.stephen.lab.model.EntityMap;
import com.stephen.lab.util.BaseDao;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EntityMapDao extends BaseDao<EntityMap>{
    List<EntityMapDto> selectDto(@Param("condition") EntityMap condition);
}
