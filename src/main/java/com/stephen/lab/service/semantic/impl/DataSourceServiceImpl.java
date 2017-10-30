package com.stephen.lab.service.semantic.impl;

import com.stephen.lab.constant.DataSourceType;
import com.stephen.lab.dao.semantic.DataSourceDao;
import com.stephen.lab.model.semantic.DataSource;
import com.stephen.lab.service.semantic.DataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataSourceServiceImpl implements DataSourceService {
    @Autowired
    private DataSourceDao dataSourceDao;

    @Override
    public List<DataSource> getAllDataSources() {
        List<DataSource> dataSources = dataSourceDao.selectAll();
        return dataSources;
    }

    @Override
    public List<DataSource> getSemanticDataSources() {
        DataSource condition = new DataSource();
        condition.setSourceType(DataSourceType.SEMANTIC);
        return dataSourceDao.select(condition);
    }

    public DataSource getDataSource(Integer source) {
        return dataSourceDao.selectByPrimaryKey(source);
    }
}
