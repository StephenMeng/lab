package com.stephen.lab.service.crawler;

import com.stephen.lab.model.crawler.CrawlError;

/**
 * Created by stephen on 2017/10/22.
 */
public interface CrawlErrorService {
    int addErrorItem(CrawlError crawlError);

    void addErrorItem(String url, Integer chinaDailyComId);
}
