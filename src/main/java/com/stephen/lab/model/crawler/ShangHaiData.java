package com.stephen.lab.model.crawler;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "crawler_shanghai_data")
public class ShangHaiData {
    @Id
    @Column(name = "data_id")
    private String dataId;
    @Column(name = "title")
    private String title;
    @Column(name = "star")
    private String star;
    @Column(name = "href")
    private String href;
    @Column(name = "view_num")
    private Integer viewNum;
    @Column(name = "download_num")
    private Integer downloadNum;
    @Column(name = "area")
    private String area;
    @Column(name = "update_date")
    private String updateDate;
    @Column(name = "data_abstract")
    private String dataAbstract;
    @Column(name = "scenarios")
    private String scenarios;
    @Column(name = "tag")
    private String tag;
    @Column(name = "keywords")
    private String keywords;
    @Column(name = "classify_country")
    private String classifyCountry;
    @Column(name = "classify_dept")
    private String classifyDept;
    @Column(name = "public_type")
    private String publicType;
    @Column(name = "pub_date")
    private String pubDate;
    @Column(name = "source")
    private String source;

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public Integer getViewNum() {
        return viewNum;
    }

    public void setViewNum(Integer viewNum) {
        this.viewNum = viewNum;
    }

    public Integer getDownloadNum() {
        return downloadNum;
    }

    public void setDownloadNum(Integer downloadNum) {
        this.downloadNum = downloadNum;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public String getDataAbstract() {
        return dataAbstract;
    }

    public void setDataAbstract(String dataAbstract) {
        this.dataAbstract = dataAbstract;
    }

    public String getScenarios() {
        return scenarios;
    }

    public String getStar() {
        return star;
    }

    public void setStar(String star) {
        this.star = star;
    }

    public void setScenarios(String scenarios) {
        this.scenarios = scenarios;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getClassifyCountry() {
        return classifyCountry;
    }

    public void setClassifyCountry(String classifyCountry) {
        this.classifyCountry = classifyCountry;
    }

    public String getClassifyDept() {
        return classifyDept;
    }

    public void setClassifyDept(String classifyDept) {
        this.classifyDept = classifyDept;
    }

    public String getPublicType() {
        return publicType;
    }

    public void setPublicType(String publicType) {
        this.publicType = publicType;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "ShangHaiData{" +
                "dataId='" + dataId + '\'' +
                ", title='" + title + '\'' +
                ", star='" + star + '\'' +
                ", href='" + href + '\'' +
                ", viewNum=" + viewNum +
                ", downloadNum=" + downloadNum +
                ", area='" + area + '\'' +
                ", updateDate='" + updateDate + '\'' +
                ", dataAbstract='" + dataAbstract + '\'' +
                ", scenarios='" + scenarios + '\'' +
                ", tag='" + tag + '\'' +
                ", keywords='" + keywords + '\'' +
                ", classifyCountry='" + classifyCountry + '\'' +
                ", classifyDept='" + classifyDept + '\'' +
                ", publicType='" + publicType + '\'' +
                ", pubDate='" + pubDate + '\'' +
                ", source='" + source + '\'' +
                '}';
    }
}
