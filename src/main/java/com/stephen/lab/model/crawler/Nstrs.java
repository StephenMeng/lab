package com.stephen.lab.model.crawler;

import javax.persistence.*;

@Entity
@Table(name = "crawler_nstrs")
public class Nstrs {
    @Id
    @Column(name = "id")
    private Integer id;
    @Basic
    @Column(name = "nstrs_id")
    private String nstrsId;
    @Basic
    @Column(name = "title")
    private String title;
    @Basic
    @Column(name = "title_en")
    private String titleEn;
    @Basic
    @Column(name = "open_range")
    private String openRange;
    @Basic
    @Column(name = "type")
    private String type;
    @Basic
    @Column(name = "date")
    private String date;
    @Basic
    @Column(name = "author")
    private String author;
    @Basic
    @Column(name = "abstract_cn")
    private String abstractCn;
    @Basic
    @Column(name = "abstract_en")
    private String abstractEn;
    @Basic
    @Column(name = "keyword_cn")
    private String keywordCn;
    @Basic
    @Column(name = "keyword_en")
    private String keywordEn;
    @Basic
    @Column(name = "page_num")
    private String pageNum;
    @Basic
    @Column(name = "lno")
    private String lno;
    @Basic
    @Column(name = "province")
    private String province;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNstrsId() {
        return nstrsId;
    }

    public void setNstrsId(String nstrs_id) {
        this.nstrsId = nstrs_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleEn() {
        return titleEn;
    }

    public void setTitleEn(String titleEn) {
        this.titleEn = titleEn;
    }

    public String getOpenRange() {
        return openRange;
    }

    public void setOpenRange(String openRange) {
        this.openRange = openRange;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAbstractCn() {
        return abstractCn;
    }

    public void setAbstractCn(String abstractCn) {
        this.abstractCn = abstractCn;
    }

    public String getAbstractEn() {
        return abstractEn;
    }

    public void setAbstractEn(String abstractEn) {
        this.abstractEn = abstractEn;
    }

    public String getKeywordCn() {
        return keywordCn;
    }

    public void setKeywordCn(String keywordCn) {
        this.keywordCn = keywordCn;
    }

    public String getKeywordEn() {
        return keywordEn;
    }

    public void setKeywordEn(String keywordEn) {
        this.keywordEn = keywordEn;
    }

    public String getPageNum() {
        return pageNum;
    }

    public void setPageNum(String pageNum) {
        this.pageNum = pageNum;
    }

    public String getLno() {
        return lno;
    }

    public void setLno(String lno) {
        this.lno = lno;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    @Override
    public String toString() {
        return "Nstrs{" +
                "nstrsId=" + nstrsId +
                ", title='" + title + '\'' +
                ", titleEn='" + titleEn + '\'' +
                ", openRange='" + openRange + '\'' +
                ", type='" + type + '\'' +
                ", date='" + date + '\'' +
                ", author='" + author + '\'' +
                ", abstractCn='" + abstractCn + '\'' +
                ", abstractEn='" + abstractEn + '\'' +
                ", keywordCn='" + keywordCn + '\'' +
                ", keywordEn='" + keywordEn + '\'' +
                ", pageNum='" + pageNum + '\'' +
                ", lno='" + lno + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Nstrs) {
            if (((Nstrs) obj).getNstrsId().equals(nstrsId)) {
                return true;
            }
        }
        return super.equals(obj);
    }
}
