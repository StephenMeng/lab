package com.stephen.lab.service;

import com.stephen.lab.model.User;

/**
 * Created by stephen on 2017/7/15.
 */
public interface UserService {
    User getUser(Integer userId);

    /**
     * 检查登陆
     *
     * @param inputUserName 输入用户名
     * @param password      输入的密码
     */

    User check(String inputUserName, String password);

    /**
     * 用户注册
     * @param user
     * @return
     */
    int signUp(User user);
}
