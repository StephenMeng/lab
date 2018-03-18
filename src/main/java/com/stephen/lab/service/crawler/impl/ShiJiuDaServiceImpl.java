package com.stephen.lab.service.crawler.impl;

import com.github.pagehelper.Page;
import com.stephen.lab.dao.crawler.ShiJiuDaDao;
import com.stephen.lab.model.crawler.ShiJiuDaMessage;
import com.stephen.lab.service.crawler.ShiJiuDaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by stephen on 2017/10/22.
 */
@Service
public class ShiJiuDaServiceImpl implements ShiJiuDaService {
    @Autowired
    private ShiJiuDaDao shiJiuDaDao;

    @Override
    public int addShiJiuDaItem(ShiJiuDaMessage shiJiuDaMessage) {
        int count = 0;
        try {
            count = shiJiuDaDao.insert(shiJiuDaMessage);
        } catch (Exception e) {

        } finally {
            return count;
        }
    }

    @Override
    public int updateSelective(ShiJiuDaMessage shiJiuDaMessage) {
        return shiJiuDaDao.updateByPrimaryKeySelective(shiJiuDaMessage);
    }

    @Override
    public List<ShiJiuDaMessage> select(ShiJiuDaMessage shiJiuDaMessage) {

        return shiJiuDaDao.select(shiJiuDaMessage);
    }

    @Override
    public List<ShiJiuDaMessage> selectAll() {
        return shiJiuDaDao.selectAll();
    }
}
