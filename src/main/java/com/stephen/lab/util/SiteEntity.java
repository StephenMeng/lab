package com.stephen.lab.util;

import com.stephen.lab.dto.Language;
import com.stephen.lab.model.User;

/**
 * Created by ZZT on 2017/7/19.
 */
public class SiteEntity {
    private User user;
    private Language language;

    public void setLanguage(Language language) {
        this.language = language;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Language getLanguage() {
        return language;
    }

    public User getUser() {
        return user;
    }
}
