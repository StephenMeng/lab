package com.stephen.lab.service.crawler;

import com.stephen.lab.model.crawler.Weibo;

/**
 * Created by stephen on 2017/10/29.
 */
public interface WeiboCrawlerService {
    int addOrUpdate(Weibo weibo);
}
