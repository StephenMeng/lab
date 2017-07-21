package com.stephen.lab.dao;

import com.stephen.lab.model.Word;
import com.stephen.lab.util.BaseDao;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WordDao extends BaseDao<Word>{
}
