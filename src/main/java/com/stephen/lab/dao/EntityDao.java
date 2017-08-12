package com.stephen.lab.dao;

import com.stephen.lab.model.Entity;
import com.stephen.lab.model.EntityRelation;
import com.stephen.lab.util.BaseDao;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EntityDao extends BaseDao<Entity>{
    List<EntityRelation> selectRelations(@Param("entityId") Integer entityId);
}
