package com.stephen.lab.service.impl;

import com.github.pagehelper.Page;
import com.stephen.lab.dao.DataSourceDao;
import com.stephen.lab.model.DataSource;
import com.stephen.lab.service.DataSourceService;
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
}
