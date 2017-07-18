package com.stephen.lab.dao;

import com.stephen.lab.util.BaseDao;
import com.stephen.lab.model.User;
import org.apache.ibatis.annotations.Mapper;
/**
 * Created by stephen on 2017/7/15.
 */
@Mapper
public interface UserDao extends BaseDao<User> {
}
