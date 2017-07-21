package com.stephen.lab.service.impl;

import com.stephen.lab.constant.JedisConstant;
import com.stephen.lab.dao.WordDao;
import com.stephen.lab.dao.WordMapDao;
import com.stephen.lab.dao.WordRelationDao;
import com.stephen.lab.dto.WordMapDto;
import com.stephen.lab.model.Word;
import com.stephen.lab.model.WordMap;
import com.stephen.lab.model.WordRelation;
import com.stephen.lab.service.WordService;
import com.stephen.lab.util.LogRecod;
import com.stephen.lab.util.jedis.JedisAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class WordServiceImpl implements WordService{
    @Autowired
    private WordMapDao wordMapDao;
    @Autowired
    private WordDao wordDao;
    @Autowired
    private WordRelationDao wordRelationDao;
    @Autowired
    private JedisAdapter jedisAdapter;
    @Override
    public List<WordMap> getWordMap(String term) {
        Word condition=new Word();
        condition.setWordName(term);
        Word word=wordDao.selectOne(condition);
        WordMap wordMapCondition=new WordMap();
        wordMapCondition.setEntityA(word.getWordId());
        List<WordMap>wordMapList=wordMapDao.select(wordMapCondition);
        return wordMapList;
    }

    @Override
    public List<WordMapDto> getWordMapDto(String term) {
        Word condition=new Word();
        condition.setWordName(term);
        Word word=wordDao.selectOne(condition);
        WordMap wordMapCondition=new WordMap();
        wordMapCondition.setEntityA(word.getWordId());
        List<WordMapDto>wordMapDtos=wordMapDao.selectDto(wordMapCondition);

        return wordMapDtos;
    }

    @Override
    public String getWordFromCache(Integer entity) {
        String key=String.format(JedisConstant.KEY_WORD,entity);
        String wordName=null;
        if(jedisAdapter.exists(key)){
            wordName=jedisAdapter.get(key);
        }else {
            Word word=new Word();
            word.setWordId(entity);
            word=wordDao.selectOne(word);
            wordName=word.getWordName();
            jedisAdapter.set(key,wordName);
        }
        return wordName;
    }

    @Override
    public String getRelationFromCache(Integer rId) {
        String key=String.format(JedisConstant.KEY_RELATION,rId);
        String relation;
        if(jedisAdapter.exists(key)){
            relation=jedisAdapter.get(key);
        }else {
            WordRelation wordRelation=new WordRelation();
            wordRelation.setRelationId(rId);
            wordRelation=wordRelationDao.selectOne(wordRelation);
            relation=wordRelation.getRelationName();
            jedisAdapter.set(key,relation);
        }
        return relation;
    }
}
