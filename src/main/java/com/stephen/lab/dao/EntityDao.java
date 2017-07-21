package com.stephen.lab.dao;

import com.stephen.lab.model.Entity;
import com.stephen.lab.util.BaseDao;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EntityDao extends BaseDao<Entity>{
}
