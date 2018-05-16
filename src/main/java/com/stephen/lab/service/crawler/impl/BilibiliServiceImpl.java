package com.stephen.lab.service.crawler.impl;

import com.stephen.lab.dao.crawler.BilibiliDao;
import com.stephen.lab.model.others.Bilibili;
import com.stephen.lab.service.crawler.BilibiliService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BilibiliServiceImpl implements BilibiliService {
    @Autowired
    private BilibiliDao bilibiliDao;

    @Override
    public int insert(Bilibili bilibili) {
        return bilibiliDao.insert(bilibili);
    }

    @Override
    public List<Bilibili> selectAll() {
        return bilibiliDao.selectAll();
    }

    @Override
    public int updateSelective(Bilibili b) {
        return bilibiliDao.updateByPrimaryKeySelective(b);
    }
}
