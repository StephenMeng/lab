package com.stephen.lab.service.crawler;

import com.github.pagehelper.Page;
import com.stephen.lab.model.crawler.ShiJiuDaMessage;

import java.util.List;

/**
 * Created by stephen on 2017/10/22.
 */
public interface ShiJiuDaService {
    int addShiJiuDaItem(ShiJiuDaMessage shiJiuDaMessage);

    int updateSelective(ShiJiuDaMessage shiJiuDaMessage);

    List<ShiJiuDaMessage> select(ShiJiuDaMessage shiJiuDaMessage);

    List<ShiJiuDaMessage> selectAll();
}
