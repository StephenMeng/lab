package com.stephen.lab.service.crawler;

import com.stephen.lab.model.crawler.Nstrs;
import com.stephen.lab.model.crawler.ShangHaiData;

import java.util.List;

public interface ShangHaiDataService {
    int add(ShangHaiData shangHaiData);

    List<ShangHaiData> selectAll();

    void updateSelective(ShangHaiData shangHaiData);

    ShangHaiData selectOne(ShangHaiData shangHaiData);
}
