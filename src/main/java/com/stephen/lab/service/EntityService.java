package com.stephen.lab.service;

import com.stephen.lab.dto.EntityMapDto;
import com.stephen.lab.model.Entity;
import com.stephen.lab.model.EntityMap;
import com.stephen.lab.model.EntityRelation;

import java.util.List;

public interface EntityService {
    /**
     * 查找三元组
     * @param word 实体
     * @param wordType 实体类别
     * @return
     */
    List<EntityMap> getWordMap(String word,Integer wordType);

    /**
     * 获得实体
     * @param term 实体名字
     * @param type 实体类型
     * @return
     */
    Entity getEntity(String term,Integer type);

    /**
     * 获得三元组
     * @param wordName 实体名字
     * @param wordType 实体类型
     * @param relationName 关系名字
     * @param relationType 关系类型
     * @return
     */
    List<EntityMap> getWordMap(String wordName,Integer wordType,String relationName,Integer relationType);

    /**
     * 获取关系
     * @param wordName 实体名字
     * @param wordType 实体类型
     * @return
     */
    List<EntityRelation>getRelations(String wordName,Integer wordType);

    /**
     * 从缓存中获取实体
     * @param entity
     * @return
     */
    Entity getWordFromCache(Integer entity);

    /**
     * 从缓存中获得关系
     * @param relation
     * @return
     */
    EntityRelation getRelationFromCache(Integer relation);
}
