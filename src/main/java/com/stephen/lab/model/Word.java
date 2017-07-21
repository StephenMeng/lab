package com.stephen.lab.model;

import javax.persistence.*;

@Entity
@Table(name = "word")
public class Word {
    @Id
    @Column(name = "word_id")
    private Integer wordId;
    private String wordName;

    public Integer getWordId() {
        return wordId;
    }
    @Basic
    @Column(name = "word_name")
    public String getWordName() {
        return wordName;
    }

    public void setWordId(Integer wordId) {
        this.wordId = wordId;
    }

    public void setWordName(String wordName) {
        this.wordName = wordName;
    }
}
