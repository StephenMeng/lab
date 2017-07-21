package com.stephen.lab.service;

import com.stephen.lab.dto.WordMapDto;
import com.stephen.lab.model.WordMap;

import java.util.List;

public interface WordService {
    List<WordMap> getWordMap(String word);

    List<WordMapDto> getWordMapDto(String term);

    String getWordFromCache(Integer entity);

    String getRelationFromCache(Integer relation);
}
