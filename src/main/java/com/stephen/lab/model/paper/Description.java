package com.stephen.lab.model.paper;

import java.io.Serializable;
import java.util.List;

public class Description implements Serializable{
    private List<String> languages;

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }
}
