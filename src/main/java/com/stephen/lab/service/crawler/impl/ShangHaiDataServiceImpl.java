package com.stephen.lab.service.crawler.impl;

import com.stephen.lab.dao.crawler.ShangHaiDataDao;
import com.stephen.lab.model.crawler.Nstrs;
import com.stephen.lab.model.crawler.ShangHaiData;
import com.stephen.lab.service.crawler.ShangHaiDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShangHaiDataServiceImpl implements ShangHaiDataService {
    @Autowired
    private ShangHaiDataDao shangHaiDataDao;

    @Override
    public int add(ShangHaiData shangHaiData) {
        return shangHaiDataDao.insert(shangHaiData);
    }

    @Override
    public List<ShangHaiData> selectAll() {
        return shangHaiDataDao.selectAll();
    }

    @Override
    public void updateSelective(ShangHaiData shangHaiData) {
        shangHaiDataDao.updateByPrimaryKeySelective(shangHaiData);
    }

    @Override
    public ShangHaiData selectOne(ShangHaiData shangHaiData) {
        return shangHaiDataDao.selectOne(shangHaiData);
    }
}
