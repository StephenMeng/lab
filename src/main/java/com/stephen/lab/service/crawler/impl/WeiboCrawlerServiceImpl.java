package com.stephen.lab.service.crawler.impl;

import com.stephen.lab.dao.crawler.WeiboDao;
import com.stephen.lab.model.crawler.Weibo;
import com.stephen.lab.service.crawler.WeiboCrawlerService;
import com.stephen.lab.util.LogRecod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by stephen on 2017/10/29.
 */
@Service
public class WeiboCrawlerServiceImpl implements WeiboCrawlerService {
    @Autowired
    private WeiboDao weiboDao;

    @Override
    public int addOrUpdate(Weibo weibo) {
        try {
            return weiboDao.insert(weibo);
        } catch (Exception e) {
            LogRecod.print("重复的数据:"+weibo.getUrl());
            LogRecod.print(weibo);
            try {
                weibo.setUpdateDate(new Date());
                return weiboDao.updateByPrimaryKeySelective(weibo);
            } catch (Exception e2) {

            }
            return 0;
        }
    }
}
