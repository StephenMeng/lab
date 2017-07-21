package com.stephen.lab.service.impl;

import com.stephen.lab.constant.JedisConstant;
import com.stephen.lab.dao.EntityDao;
import com.stephen.lab.dao.EntityMapDao;
import com.stephen.lab.dao.EntityRelationDao;
import com.stephen.lab.dto.EntityMapDto;
import com.stephen.lab.model.Entity;
import com.stephen.lab.model.EntityMap;
import com.stephen.lab.model.EntityRelation;
import com.stephen.lab.service.WordService;
import com.stephen.lab.util.jedis.JedisAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WordServiceImpl implements WordService {
    @Autowired
    private EntityMapDao entityMapDao;
    @Autowired
    private EntityDao entityDao;
    @Autowired
    private EntityRelationDao entityRelationDao;
    @Autowired
    private JedisAdapter jedisAdapter;

    @Override
    public List<EntityMap> getWordMap(String term) {
        Entity condition = new Entity();
        condition.setEntityName(term);
        Entity entity = entityDao.selectOne(condition);
        EntityMap entityMap = new EntityMap();
        entityMap.setEntityA(entity.getEntityId());
        return entityMapDao.select(entityMap);
    }

    @Override
    public List<EntityMapDto> getWordMapDto(String term) {
        Entity condition = new Entity();
        condition.setEntityName(term);
        Entity entity = entityDao.selectOne(condition);
        EntityMap entityMap = new EntityMap();
        entityMap.setEntityA(entity.getEntityId());
        return entityMapDao.selectDto(entityMap);
    }

    @Override
    public String getWordFromCache(Integer eid) {
        String key = String.format(JedisConstant.KEY_WORD, eid);
        String entityName;
        if (jedisAdapter.exists(key)) {
            entityName = jedisAdapter.get(key);
        } else {
            Entity entity = new Entity();
            entity.setEntityId(eid);
            entity = entityDao.selectByPrimaryKey(entity);
            entityName = entity.getEntityName();
            jedisAdapter.set(key, entityName);
        }
        return entityName;
    }

    @Override
    public String getRelationFromCache(Integer rId) {
        String key = String.format(JedisConstant.KEY_RELATION, rId);
        String relation;
        if (jedisAdapter.exists(key)) {
            relation = jedisAdapter.get(key);
        } else {
            EntityRelation entityRelation = new EntityRelation();
            entityRelation.setRelationId(rId);
            entityRelation = entityRelationDao.selectOne(entityRelation);
            relation = entityRelation.getRelationName();
            jedisAdapter.set(key, relation);
        }
        return relation;
    }
}
