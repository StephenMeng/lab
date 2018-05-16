package com.stephen.lab.service.crawler.impl;

import com.stephen.lab.dao.crawler.BDanmuDao;
import com.stephen.lab.dao.crawler.BilibiliDao;
import com.stephen.lab.model.others.BDanmu;
import com.stephen.lab.model.others.Bilibili;
import com.stephen.lab.service.crawler.BDanmuService;
import com.stephen.lab.service.crawler.BilibiliService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BDanmuServiceImpl implements BDanmuService {
    @Autowired
    private BDanmuDao danmuDao;


    @Override
    public int insert(BDanmu bDanmu) {
        return danmuDao.insert(bDanmu);
    }
}
