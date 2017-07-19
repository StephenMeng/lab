package com.stephen.lab.util;

import com.stephen.lab.model.User;

/**
 * Created by ZZT on 2017/7/19.
 */
public class Holder {
    private static ThreadLocal<SiteEntity>threadLocal=new ThreadLocal<SiteEntity>();
    public static void setUser(User user){
        SiteEntity entity=threadLocal.get();
        if(entity==null){
            entity=new SiteEntity();
        }
        entity.setUser(user);
        threadLocal.set(entity);
    }
    public static User getUser(){
        SiteEntity entity=threadLocal.get();
        return entity.getUser();
    }
}
