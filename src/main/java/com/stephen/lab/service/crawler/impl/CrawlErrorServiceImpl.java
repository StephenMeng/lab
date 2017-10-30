package com.stephen.lab.service.crawler.impl;

import com.stephen.lab.constant.crawler.ErrorConstant;
import com.stephen.lab.constant.crawler.UrlConstant;
import com.stephen.lab.dao.crawler.CrawlErrorDao;
import com.stephen.lab.model.crawler.CrawlError;
import com.stephen.lab.service.crawler.CrawlErrorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by stephen on 2017/10/22.
 */
@Service
public class CrawlErrorServiceImpl implements CrawlErrorService {
    @Autowired
    private CrawlErrorDao crawlErrorDao;

    @Override
    public int addErrorItem(CrawlError crawlError) {
        return crawlErrorDao.insert(crawlError);
    }

    @Override
    public void addErrorItem(String url, Integer chinaDailyComId) {
        CrawlError crawlError = new CrawlError();
        crawlError.setCreateDate(new Date());
        crawlError.setErrorHref(url);
        crawlError.setSourceId(chinaDailyComId);
        crawlError.setStatus(ErrorConstant.ERROR);
        addErrorItem(crawlError);
    }
}
