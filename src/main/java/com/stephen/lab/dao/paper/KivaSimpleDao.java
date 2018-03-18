package com.stephen.lab.dao.paper;

import com.stephen.lab.model.paper.KivaSimple;
import com.stephen.lab.util.BaseDao;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface KivaSimpleDao extends BaseDao<KivaSimple> {
    List<Long> selectLikeTag(String tag);
}
