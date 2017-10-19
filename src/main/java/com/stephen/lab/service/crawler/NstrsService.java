package com.stephen.lab.service.crawler;

import com.stephen.lab.model.crawler.Nstrs;

import java.util.List;

public interface NstrsService {
    int add(Nstrs nstrs);

    List<Nstrs> selectAll();

    void updateSelective(Nstrs nstrs);

    Nstrs selectOne(Nstrs nstrs);
}
