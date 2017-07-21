package com.stephen.lab.controller;

import com.stephen.lab.dto.EntityMapDto;
import com.stephen.lab.model.EntityMap;
import com.stephen.lab.service.WordService;
import com.stephen.lab.util.Response;
import com.stephen.lab.util.jedis.JedisAdapter;
import com.stephen.lab.util.solr.SolrAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stephen on 2017/7/15.
 */
@Controller
@RequestMapping("word")
public class WordController {
    @Autowired
    private WordService wordService;
    @Autowired
    private JedisAdapter jedisAdapter;
    @Autowired
    private SolrAdapter solrAdapter;
    @RequestMapping("get")
    @ResponseBody
    public Response getUser(@RequestParam("term") String term) {
        term="标签聚类";
        List<EntityMap>wordMapList=wordService.getWordMap(term);
        List<EntityMapDto> entityMapDtos =new ArrayList<>();
        for(EntityMap wordMap:wordMapList){
            EntityMapDto entityMapDto =new EntityMapDto();
            entityMapDto.setMapId(wordMap.getMapId());
            entityMapDto.setEntityA(wordService.getWordFromCache(wordMap.getEntityA()));
            entityMapDto.setRelation(wordService.getRelationFromCache(wordMap.getRelation()));
            entityMapDto.setEntityB(wordService.getWordFromCache(wordMap.getEntityB()));
            entityMapDtos.add(entityMapDto);
        }
        return Response.success(entityMapDtos);
    }

}

