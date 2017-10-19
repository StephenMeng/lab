package com.stephen.lab.model;

import com.stephen.lab.dto.UserDto;

import javax.persistence.*;
import javax.persistence.Entity;

/**
 * Created by stephen on 2017/7/15.
 */
@Entity
@Table(name = "user")
public class User {
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;
    @Basic
    @Column(name = "user_name")
    private String userName;
    private String password;

    public Integer getUserId() {
        return userId;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                '}';
    }

    public UserDto modelToDto() {
        UserDto userDto = new UserDto();
        userDto.setUserId(userId);
        userDto.setUserName(userName);
        return userDto;
    }
}
