package com.stephen.lab.controller.semantic;

import com.stephen.lab.constant.semantic.ResultEnum;
import com.stephen.lab.dto.semantic.EntityDto;
import com.stephen.lab.dto.semantic.EntityMapDto;
import com.stephen.lab.dto.semantic.RelationWithEntity;
import com.stephen.lab.model.semantic.Entity;
import com.stephen.lab.model.semantic.EntityMap;
import com.stephen.lab.model.semantic.EntityRelation;
import com.stephen.lab.service.semantic.EntityService;
import com.stephen.lab.util.ListUtil;
import com.stephen.lab.util.LogRecod;
import com.stephen.lab.util.Response;
import com.stephen.lab.util.jedis.JedisAdapter;
import com.stephen.lab.util.solr.SolrAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by stephen on 2017/7/15.
 */
@Controller
@RequestMapping("semantic/entity")
public class EntityController {
    @Autowired
    private EntityService entityService;
    @Autowired
    private JedisAdapter jedisAdapter;
    @Autowired
    private SolrAdapter solrAdapter;

    @RequestMapping("")
    public ModelAndView getEntityMAV() {
        return new ModelAndView("entity");
    }


    @RequestMapping("exist")
    @ResponseBody
    public Response esixt(@RequestParam("term") String term) {
        Entity entity = entityService.getEntity(term);
        if (entity == null || entity.getEntityId() == null) {
           return Response.success(false);
        }
        return Response.success(true);
    }

    @RequestMapping("get")
    @ResponseBody
    public Response getUser(@RequestParam("term") String term) {
        Entity entity = entityService.getEntity(term);
        if (entity == null) {
            return Response.error(ResultEnum.FAIL_PARAM_WRONG.getCode(), "there is not this entity's info", "查无此Enitity");
        }
        EntityDto entityADto = EntityDto.modelToDto(entity);
        entityADto.setIsEntity(true);
        List<EntityMap> wordMapListA = entityService.getWordMapForEntityA(term);
        LogRecod.print(wordMapListA);
        List<EntityMap> wordMapListB = entityService.getWordMapForEntityB(term);
        LogRecod.print(wordMapListB);
        EntityMapDto entityMapDto = new EntityMapDto();
        entityMapDto.setEntity(entityADto);

        List<RelationWithEntity> relationWithEntities = new ArrayList<>();
        for (EntityMap wordMap : wordMapListA) {
            Entity entityB = null;
            EntityDto entityDtoB = null;
            if (wordMap.getEntityB() == null) {
                entityB = new Entity();
                entityB.setEntityType("comment");
                entityB.setEntityName(wordMap.getText());
                entityB.setEntityId(-1l);
                entityDtoB = EntityDto.modelToDto(entityB);
            } else {
                entityB = entityService.getWordFromCache(wordMap.getEntityB());
                entityDtoB = EntityDto.modelToDto(entityB);
                entityDtoB.setIsEntity(true);
            }
            LogRecod.print(entityDtoB.getEntityName());
            EntityRelation entityRelation = entityService.getRelationFromCache(wordMap.getRelation());
            boolean contains = false;
            for (RelationWithEntity relationWithEntity : relationWithEntities) {
                if (relationWithEntity.getRelationId().equals(entityRelation.getRelationId())) {
                    relationWithEntity.getEntities().add(entityDtoB);
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                RelationWithEntity relationWithEntity = new RelationWithEntity();
                relationWithEntity.setRelationId(entityRelation.getRelationId());
                relationWithEntity.setRelationName(entityRelation.getRelationName());
                relationWithEntity.setRelationType(entityRelation.getRelationType());

                relationWithEntity.setEntities(ListUtil.newItemsToList(entityDtoB));
                relationWithEntities.add(relationWithEntity);
            }

        }

        for (EntityMap wordMap : wordMapListB) {
            Entity entityA = entityService.getWordFromCache(wordMap.getEntityA());
            EntityDto temp=EntityDto.modelToDto(entityA);
            EntityRelation entityRelation = entityService.getRelationFromCache(wordMap.getRelation());
            entityRelation.setRelationType(1);
            boolean contains = false;
            for (RelationWithEntity relationWithEntity : relationWithEntities) {
                if (relationWithEntity.getRelationId().equals(entityRelation.getRelationId())
                        && relationWithEntity.getRelationType().equals(entityRelation.getRelationType())) {
                    relationWithEntity.getEntities().add(temp);
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                RelationWithEntity relationWithEntity = new RelationWithEntity();
                relationWithEntity.setRelationId(entityRelation.getRelationId());
                relationWithEntity.setRelationName(entityRelation.getRelationNamePassive());
                relationWithEntity.setRelationType(entityRelation.getRelationType());
                relationWithEntity.setEntities(ListUtil.newItemsToList(temp));
                relationWithEntities.add(relationWithEntity);
            }

        }

        entityMapDto.setRelationWithEntities(relationWithEntities);
        return Response.success(entityMapDto);
    }

    @RequestMapping("type")
    @ResponseBody
    public Response getEntityTypes(@RequestParam("term") String term) {
        List<Entity> entity = entityService.getEntityTypes(term);
        List<EntityDto> entityDtos = entity.stream().map(EntityDto::new).collect(Collectors.toList());
        entityDtos.forEach(entityDto -> {
        });
        return Response.success(entityDtos);
    }

    @RequestMapping("relation")
    @ResponseBody
    public Response getEntityRelation(@RequestParam("entityId") Long entityId) {
        List<EntityRelation> relations = entityService.getRelations(entityId);
        return Response.success(relations);
    }

    @RequestMapping("map-entity")
    @ResponseBody
    public Response getMapEntity(@RequestParam("entityId") Long entityId, @RequestParam("relationId") Long relationId) {
        List<Entity> entities = entityService.getEntity(entityId, relationId);
        return Response.success(entities);
    }
}

