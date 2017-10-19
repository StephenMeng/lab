package com.stephen.lab.service.impl;

import com.stephen.lab.dao.UserDao;
import com.stephen.lab.model.User;
import com.stephen.lab.service.UserService;
import com.stephen.lab.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by stephen on 2017/7/15.
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;

    @Override
    public User getUser(Integer userId) {
        User user = new User();
        user.setUserId(userId);
        user = userDao.selectOne(user);
        return user;
    }

    @Override
    public User check(String inputUserName, String password) {
        User user = new User();
        user.setUserName(inputUserName);
        user.setPassword(SecurityUtil.md5(password));
        return userDao.selectOne(user);
    }

    @Override
    public int signUp(User user) {
        return userDao.insert(user);
    }
}
