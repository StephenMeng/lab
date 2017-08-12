package com.stephen.lab.dao;

import com.stephen.lab.model.Paper;
import com.stephen.lab.util.BaseDao;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaperDao extends BaseDao<Paper>{
}
