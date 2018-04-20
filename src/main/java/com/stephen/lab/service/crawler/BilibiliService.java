package com.stephen.lab.service.crawler;

import com.stephen.lab.model.others.Bilibili;

import java.util.List;

public interface BilibiliService {
    int insert(Bilibili bilibili);

    List<Bilibili> selectAll();

    int updateSelective(Bilibili b);
}
