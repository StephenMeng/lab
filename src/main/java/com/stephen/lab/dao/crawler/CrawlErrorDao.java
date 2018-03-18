package com.stephen.lab.dao.crawler;

import com.stephen.lab.model.crawler.CrawlError;
import com.stephen.lab.util.BaseDao;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created by stephen on 2017/10/22.
 */
@Mapper
public interface CrawlErrorDao extends BaseDao<CrawlError> {

}
