package com.stephen.lab.controller;

import com.stephen.lab.constant.JedisConstant;
import com.stephen.lab.dto.WordMapDto;
import com.stephen.lab.model.WordMap;
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
        List<WordMap>wordMapList=wordService.getWordMap(term);
        List<WordMapDto>wordMapDtos=new ArrayList<>();
        for(WordMap wordMap:wordMapList){
            WordMapDto wordMapDto=new WordMapDto();
            wordMapDto.setMapId(wordMap.getMapId());
            wordMapDto.setEntityA(wordService.getWordFromCache(wordMap.getEntityA()));
            wordMapDto.setRelation(wordService.getRelationFromCache(wordMap.getRelation()));
            wordMapDto.setEntityB(wordService.getWordFromCache(wordMap.getEntityB()));
            wordMapDtos.add(wordMapDto);
        }
        return Response.success(wordMapDtos);
    }

}

