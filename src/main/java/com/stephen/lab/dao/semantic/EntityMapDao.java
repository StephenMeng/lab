package com.stephen.lab.dao.semantic;

import com.stephen.lab.dto.semantic.EntityMapDto;
import com.stephen.lab.model.semantic.EntityMap;
import com.stephen.lab.util.BaseDao;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EntityMapDao extends BaseDao<EntityMap>{
    List<EntityMapDto> selectDto(@Param("condition") EntityMap condition);

    void insertIfNotExist(EntityMap entityMap);
}
