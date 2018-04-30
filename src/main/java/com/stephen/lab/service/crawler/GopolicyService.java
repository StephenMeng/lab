package com.stephen.lab.service.crawler;

import com.stephen.lab.model.crawler.Gopolicy;

import java.util.List;

public interface GopolicyService {
    int insert(Gopolicy gopolicy);

    int updateSelective(Gopolicy gopolicy);

    List<Gopolicy> selectAll();

    List<Gopolicy> selectNullTitle();

    List<Gopolicy> selectNullFullText();
}
