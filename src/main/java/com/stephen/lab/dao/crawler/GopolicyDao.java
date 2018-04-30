package com.stephen.lab.dao.crawler;

import com.stephen.lab.model.crawler.Gopolicy;
import com.stephen.lab.util.BaseDao;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GopolicyDao extends BaseDao<Gopolicy> {
    List<Gopolicy> selectNullTitle();

    List<Gopolicy> selectNullFullText();
}
