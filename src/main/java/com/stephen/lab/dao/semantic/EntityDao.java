package com.stephen.lab.dao.semantic;

import com.stephen.lab.model.semantic.Entity;
import com.stephen.lab.model.semantic.EntityRelation;
import com.stephen.lab.util.BaseDao;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EntityDao extends BaseDao<Entity> {
    List<EntityRelation> selectRelations(@Param("entityId") Long entityId);

    Long insertReturnId(Entity entity);

    Entity selectByName(@Param("entityName") String entityName);
}
