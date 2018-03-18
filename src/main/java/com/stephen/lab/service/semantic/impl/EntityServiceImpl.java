package com.stephen.lab.service.semantic.impl;

import com.alibaba.fastjson.JSON;
import com.stephen.lab.constant.semantic.JedisConstant;
import com.stephen.lab.dao.semantic.EntityDao;
import com.stephen.lab.dao.semantic.EntityMapDao;
import com.stephen.lab.dao.semantic.EntityRelationDao;
import com.stephen.lab.dto.semantic.EntityGenIdDto;
import com.stephen.lab.model.semantic.Entity;
import com.stephen.lab.model.semantic.EntityMap;
import com.stephen.lab.model.semantic.EntityRelation;
import com.stephen.lab.service.semantic.EntityService;
import com.stephen.lab.util.ListUtil;
import com.stephen.lab.util.LogRecod;
import com.stephen.lab.util.jedis.JedisAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    public List<EntityMap> getWordMapForEntityA(String term) {
        Entity entity = getEntity(term);
        LogRecod.print(entity);
        EntityMap entityMap = new EntityMap();
        entityMap.setEntityA(entity.getEntityId());
        return entityMapDao.select(entityMap);
    }

    @Override
    public List<EntityMap> getWordMapForEntityB(String word) {
        Entity entity = getEntity(word);
        EntityMap entityMap = new EntityMap();
        entityMap.setEntityB(entity.getEntityId());
        return entityMapDao.select(entityMap);
    }

    @Override
    public Entity getEntity(String term) {
        Entity condition = new Entity();
        condition.setEntityName(term);
        return entityDao.selectByName(term);
    }

    @Override
    public List<EntityRelation> getRelations(String wordName, Integer wordType) {
        Entity entity = getEntity(wordName);
        List<EntityRelation> relations = entityDao.selectRelations(entity.getEntityId());
        return relations;
    }

    @Override
    public List<EntityMap> getWordMap(String wordName, Integer wordType, String relationName, Integer relationType) {
        Entity entity = getEntity(wordName);
        EntityMap entityMap = new EntityMap();
        entityMap.setEntityA(entity.getEntityId());
        EntityRelation relation = new EntityRelation();
        relation.setRelationName(relationName);
        relation.setRelationType(relationType);
        relation = entityRelationDao.selectOne(relation);
        entityMap.setRelation(relation.getRelationId());
        return entityMapDao.select(entityMap);
    }

    @Override
    public Entity getWordFromCache(Long eid) {
        String key = String.format(JedisConstant.KEY_WORD, eid);
        Entity entity;
        if (jedisAdapter.exists(key)) {
            String entityStr = jedisAdapter.get(key);
            entity = JSON.parseObject(entityStr, Entity.class);
        } else {
            Entity condition = new Entity();
            condition.setEntityId(eid);
            entity = entityDao.selectByPrimaryKey(condition);
            jedisAdapter.set(key, JSON.toJSONString(entity));
        }
        return entity;
    }

    @Override
    public EntityRelation getRelationFromCache(Long rId) {
        String key = String.format(JedisConstant.KEY_RELATION, rId);
        EntityRelation relation;
        if (jedisAdapter.exists(key)) {
            String relationStr = jedisAdapter.get(key);
            relation = JSON.parseObject(relationStr, EntityRelation.class);
        } else {
            EntityRelation entityRelation = new EntityRelation();
            entityRelation.setRelationId(rId);
            entityRelation = entityRelationDao.selectOne(entityRelation);
            relation = entityRelation;
            jedisAdapter.set(key, JSON.toJSONString(relation));
        }
        return relation;
    }

    @Override
    public List<Entity> getEntityTypes(String term) {
        Entity entity = new Entity();
        entity.setEntityName(term);
        return entityDao.select(entity);
    }

    @Override
    public List<EntityRelation> getRelations(Long entityId) {
        EntityMap entityMap = new EntityMap();
        entityMap.setEntityA(entityId);
        List<EntityMap> entityMaps = entityMapDao.select(entityMap);
        List<EntityRelation> entityRelations = new ArrayList<>();
        entityMaps.forEach(em -> {
            EntityRelation relation = entityRelationDao.selectByPrimaryKey(em.getRelation());
            if (!entityRelations.contains(relation)) {
                entityRelations.add(relation);
            }
        });
        return entityRelations;
    }

    @Override
    public List<Entity> getEntity(Long entityId, Long relationId) {
        EntityMap entityMap = new EntityMap();
        entityMap.setEntityA(entityId);
        entityMap.setRelation(relationId);
        List<EntityMap> entityMaps = entityMapDao.select(entityMap);
        List<Entity> entities = new ArrayList<>();
        entityMaps.forEach(em -> {
            Entity entity = entityDao.selectByPrimaryKey(em.getEntityB());
            entities.add(entity);
        });
        return entities;
    }

    @Override
    public Entity getEntity(Entity entity) {
        return entityDao.selectOne(entity);
    }

    @Override
    public Long addEntity(String entityName, List<String> entityType) {
        LogRecod.print(entityName);
        Entity entity = new Entity();
        entity.setEntityName(entityName);
        entity.setEntityType(ListUtil.ListToString(entityType, ";"));
        entityDao.insertReturnId(entity);
        LogRecod.print(entity);
        return entity.getEntityId();
    }

    @Override
    public void addEntityMap(Long entityId, List<String> entities, String relation, boolean ifCreateEntityB, List<EntityGenIdDto> genIdDtos) {

        EntityRelation entityRelation = entityRelationDao.selectByName(relation);
        if (entityRelation == null) {
            entityRelation = new EntityRelation();
            entityRelation.setRelationName(relation);
            entityRelationDao.insertReturnId(entityRelation);
        }
        for (String entityStr : entities) {
            EntityMap entityMap = new EntityMap();
            entityMap.setEntityA(entityId);
            String entityB = null;
            if (ifCreateEntityB) {
                entityB = JSON.parseObject(entityStr).getString("id");
                if (entityB.contains("genid")) {
                    for (EntityGenIdDto genIdDto : genIdDtos) {
                        if (genIdDto.getGenId().equals(entityB)) {
                            entityB = genIdDto.getExt();
                            entityMap.setRelation(genIdDto.getRelationId());
                            break;
                        }
                    }
                } else {
                    entityMap.setRelation(entityRelation.getRelationId());
                }
            } else {
                entityB = JSON.parseObject(entityStr).getString("value");
                entityMap.setRelation(entityRelation.getRelationId());
            }

            if (ifCreateEntityB) {
                Entity entity = getEntity(entityB);
                if (entity == null) {
                    entity = new Entity();
                    entity.setEntityName(entityB);
                    entityDao.insertReturnId(entity);
                }
                entityMap.setEntityB(entity.getEntityId());
            } else {
                entityMap.setText(entityB);
            }
            int count = entityMapDao.selectCount(entityMap);
            if (count < 1) {
                entityMapDao.insert(entityMap);
            }
        }
    }

    @Override
    public Long searchOrAddRelation(String relation) {
        LogRecod.print(relation);
        EntityRelation result = entityRelationDao.selectByName(relation);
        if (result != null) {
            return result.getRelationId();
        }
        result = new EntityRelation();
        result.setRelationName(relation);
        entityRelationDao.insertReturnId(result);
        return result.getRelationId();
    }
}
