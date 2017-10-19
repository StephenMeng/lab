package com.stephen.lab.service.semantic;

import com.stephen.lab.dto.semantic.EntityGenIdDto;
import com.stephen.lab.model.semantic.Entity;
import com.stephen.lab.model.semantic.EntityMap;
import com.stephen.lab.model.semantic.EntityRelation;

import java.util.List;

public interface EntityService {
    /**
     * 以实体A的身份查找三元组
     *
     * @param word 实体
     * @return
     */
    List<EntityMap> getWordMapForEntityA(String word);
    /**
     * 以实体B的身份查找三元组
     *
     * @param word 实体
     * @return
     */
    List<EntityMap> getWordMapForEntityB(String word);


    /**
     * 获得实体
     *
     * @param term 实体名字
     * @return
     */
    Entity getEntity(String term);

    /**
     * 获得三元组
     *
     * @param wordName     实体名字
     * @param wordType     实体类型
     * @param relationName 关系名字
     * @param relationType 关系类型
     * @return
     */
    List<EntityMap> getWordMap(String wordName, Integer wordType, String relationName, Integer relationType);

    /**
     * 获取关系
     *
     * @param entityId 实体名字
     * @param wordType 实体类型
     * @return
     */
    List<EntityRelation> getRelations(String entityId, Integer wordType);

    /**
     * 从缓存中获取实体
     *
     * @param entity
     * @return
     */
    Entity getWordFromCache(Long entity);

    /**
     * 从缓存中获得关系
     *
     * @param relation
     * @return
     */
    EntityRelation getRelationFromCache(Long relation);

    List<Entity> getEntityTypes(String term);

    List<EntityRelation> getRelations(Long entityId);

    List<Entity> getEntity(Long entityId, Long relationId);

    Entity getEntity(Entity entity);

    Long addEntity(String entityName, List<String> entityType);

    void addEntityMap(Long entityId, List<String> relations, String entityB, boolean ifCreateEntityB, List<EntityGenIdDto> genIdDtos);

    Long searchOrAddRelation(String relation);
}
