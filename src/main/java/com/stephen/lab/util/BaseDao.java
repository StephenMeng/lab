package com.stephen.lab.util;

import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

/**
 * Created by stephen on 2017/7/15.
 */
public interface BaseDao<T> extends Mapper<T>,MySqlMapper<T> {
}
