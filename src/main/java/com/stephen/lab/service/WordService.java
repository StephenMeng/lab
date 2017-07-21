package com.stephen.lab.service;

import com.stephen.lab.dto.EntityMapDto;
import com.stephen.lab.model.EntityMap;

import java.util.List;

public interface WordService {
    List<EntityMap> getWordMap(String word);

    List<EntityMapDto> getWordMapDto(String term);

    String getWordFromCache(Integer entity);

    String getRelationFromCache(Integer relation);
}
