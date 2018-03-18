package com.stephen.lab.dao.semantic;

import com.stephen.lab.model.semantic.EntityRelation;
import com.stephen.lab.util.BaseDao;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EntityRelationDao extends BaseDao<EntityRelation> {

    EntityRelation selectByName(@Param("relationName") String relationName);

    void insertReturnId(EntityRelation entityRelation);
}
