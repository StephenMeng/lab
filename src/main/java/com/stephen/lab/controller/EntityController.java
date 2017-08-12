package com.stephen.lab.controller;

import com.stephen.lab.constant.EntityType;
import com.stephen.lab.dto.EntityMapDto;
import com.stephen.lab.model.EntityMap;
import com.stephen.lab.service.EntityService;
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

/**
 * Created by stephen on 2017/7/15.
 */
@Controller
@RequestMapping("entity")
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

    @RequestMapping("get")
    @ResponseBody
    public Response getUser(@RequestParam("term") String term) {
        term = "标签聚类";
        List<EntityMap> wordMapList = entityService.getWordMap(term, EntityType.CLASS);
        List<EntityMapDto> entityMapDtos = new ArrayList<>();
        for (EntityMap wordMap : wordMapList) {
            EntityMapDto entityMapDto = new EntityMapDto();
            entityMapDto.setMapId(wordMap.getMapId());
            entityMapDto.setEntityA(entityService.getWordFromCache(wordMap.getEntityA()).getEntityName());
            entityMapDto.setRelation(entityService.getRelationFromCache(wordMap.getRelation()).getRelationName());
            entityMapDto.setEntityB(entityService.getWordFromCache(wordMap.getEntityB()).getEntityName());
            entityMapDtos.add(entityMapDto);
        }
        return Response.success(entityMapDtos);
    }

}

