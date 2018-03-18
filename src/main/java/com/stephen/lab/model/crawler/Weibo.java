package com.stephen.lab.model.crawler;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by stephen on 2017/10/29.
 */
@Entity
@Table(name = "crawl_weibo")
public class Weibo {

    @Id
    @Column(name = "url")
    private String url;
    @Column(name = "mid")
    private String mid;
    @Column(name = "user_name")
    private String userName;
    @Column(name = "user_url")
    private String userUrl;
    @Column(name = "user_avatar_url")
    private String userAvatarUrl;
    @Column(name = "v_icon")
    private String VIcon;
    @Column(name = "v_level")
    private String VLevel;
    @Column(name = "pub_date")
    private String pubDate;
    @Column(name = "source")
    private String source;
    @Column(name = "content")
    private String content;
    @Column(name = "full_content_param")
    private String fullContentParam;
    @Column(name = "picture_urls")
    private String pictureUrls;
    @Column(name = "share_num")
    private String shareNum;
    @Column(name = "comment_num")
    private String commentNum;
    @Column(name = "like_num")
    private String likeNum;
    @Column(name = "create_date")
    private Date createDate;
    @Column(name = "update_date")
    private Date updateDate;

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserUrl() {
        return userUrl;
    }

    public void setUserUrl(String userUrl) {
        this.userUrl = userUrl;
    }

    public String getUserAvatarUrl() {
        return userAvatarUrl;
    }

    public void setUserAvatarUrl(String userAvatarUrl) {
        this.userAvatarUrl = userAvatarUrl;
    }

    public String getVIcon() {
        return VIcon;
    }

    public void setVIcon(String VIcon) {
        this.VIcon = VIcon;
    }

    public String getVLevel() {
        return VLevel;
    }

    public void setVLevel(String VLevel) {
        this.VLevel = VLevel;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFullContentParam() {
        return fullContentParam;
    }

    public void setFullContentParam(String fullContentParam) {
        this.fullContentParam = fullContentParam;
    }

    public String getPictureUrls() {
        return pictureUrls;
    }

    public void setPictureUrls(String pictureUrls) {
        this.pictureUrls = pictureUrls;
    }

    public String getShareNum() {
        return shareNum;
    }

    public void setShareNum(String shareNum) {
        this.shareNum = shareNum;
    }

    public String getCommentNum() {
        return commentNum;
    }

    public void setCommentNum(String commentNum) {
        this.commentNum = commentNum;
    }

    public String getLikeNum() {
        return likeNum;
    }

    public void setLikeNum(String likeNum) {
        this.likeNum = likeNum;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public String toString() {
        return "Weibo{" +
                "url=" + url +
                ", userName='" + userName + '\'' +
                ", userUrl='" + userUrl + '\'' +
                ", userAvatarUrl='" + userAvatarUrl + '\'' +
                ", VIcon='" + VIcon + '\'' +
                ", VLevel='" + VLevel + '\'' +
                ", pubDate='" + pubDate + '\'' +
                ", source='" + source + '\'' +
                ", content='" + content + '\'' +
                ", fullContentParam='" + fullContentParam + '\'' +
                ", pictureUrls='" + pictureUrls + '\'' +
                ", shareNum='" + shareNum + '\'' +
                ", commentNum='" + commentNum + '\'' +
                ", likeNum='" + likeNum + '\'' +
                '}';
    }
}
