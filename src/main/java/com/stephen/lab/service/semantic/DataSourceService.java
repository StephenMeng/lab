package com.stephen.lab.service.semantic;

import com.stephen.lab.model.semantic.DataSource;

import java.util.List;

/**
 * Created by stephen on 2017/7/15.
 */
public interface DataSourceService {
    List<DataSource> getAllDataSources();

    List<DataSource> getSemanticDataSources();
}
