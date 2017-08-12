package com.stephen.lab.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.stephen.lab.constant.JedisConstant;
import com.stephen.lab.dao.EntityDao;
import com.stephen.lab.dao.EntityMapDao;
import com.stephen.lab.dao.EntityRelationDao;
import com.stephen.lab.dto.EntityMapDto;
import com.stephen.lab.model.Entity;
import com.stephen.lab.model.EntityMap;
import com.stephen.lab.model.EntityRelation;
import com.stephen.lab.service.EntityService;
import com.stephen.lab.util.jedis.JedisAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EntityServiceImpl implements EntityService {
    @Autowired
    private EntityMapDao entityMapDao;
    @Autowired
    private EntityDao entityDao;
    @Autowired
    private EntityRelationDao entityRelationDao;
    @Autowired
    private JedisAdapter jedisAdapter;

    @Override
    public List<EntityMap> getWordMap(String term,Integer wordType) {
        Entity entity=getEntity(term,wordType);
        EntityMap entityMap = new EntityMap();
        entityMap.setEntityA(entity.getEntityId());
        return entityMapDao.select(entityMap);
    }

    @Override
    public Entity getEntity(String term, Integer type) {
        Entity condition = new Entity();
        condition.setEntityName(term);
        condition.setEntityType(type);
        return entityDao.selectOne(condition);
    }

    @Override
    public List<EntityRelation> getRelations(String wordName, Integer wordType) {
       Entity entity=getEntity(wordName,wordType);
       List<EntityRelation>relations=entityDao.selectRelations(entity.getEntityId());
        return relations;
    }

    @Override
    public List<EntityMap> getWordMap(String wordName,Integer wordType, String relationName,Integer relationType) {
        Entity entity = getEntity(wordName,wordType);
        EntityMap entityMap = new EntityMap();
        entityMap.setEntityA(entity.getEntityId());
        EntityRelation relation=new EntityRelation();
        relation.setRelationName(relationName);
        relation.setRelationType(relationType);
        relation=entityRelationDao.selectOne(relation);
        entityMap.setRelation(relation.getRelationId());
        return entityMapDao.select(entityMap);
    }

    @Override
    public Entity getWordFromCache(Integer eid) {
        String key = String.format(JedisConstant.KEY_WORD, eid);
        Entity entity;
        if (jedisAdapter.exists(key)) {
            String entityStr = jedisAdapter.get(key);
            entity=JSON.parseObject(entityStr,Entity.class);
        } else {
            Entity condition = new Entity();
            condition.setEntityId(eid);
            entity = entityDao.selectByPrimaryKey(condition);
            jedisAdapter.set(key, JSON.toJSONString(entity));
        }
        return entity;
    }

    @Override
    public EntityRelation getRelationFromCache(Integer rId) {
        String key = String.format(JedisConstant.KEY_RELATION, rId);
        EntityRelation relation;
        if (jedisAdapter.exists(key)) {
            String relationStr = jedisAdapter.get(key);
            relation= JSON.parseObject(relationStr,EntityRelation.class);
        } else {
            EntityRelation entityRelation = new EntityRelation();
            entityRelation.setRelationId(rId);
            entityRelation = entityRelationDao.selectOne(entityRelation);
            relation = entityRelation;
            jedisAdapter.set(key, JSON.toJSONString(relation));
        }
        return relation;
    }
}
