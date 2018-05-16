package com.stephen.lab.model.others;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "crawl_bilibili")
public class Bilibili {
    @Id
    @Column(name = "aid")
    private Long aid;
    @Column(name = "arcran")
    private String arcran;
    @Column(name = "arcurl")
    private String arcurl;
    @Column(name = "author")
    private String author;
    @Column(name = "badgepay")
    private Boolean badgepay;
    @Column(name = "description")
    private String description;
    @Column(name = "duration")
    private String duration;
    @Column(name = "favorites")
    private Integer favorites;
    @Column(name = "hit_columns")
    private String hit_columns;
    @Column(name = "id")
    private Long id;
    @Column(name = "mid")
    private Long mid;
    @Column(name = "pic")
    private String pic;
    @Column(name = "play")
    private Long play;
    @Column(name = "pubdate")
    private Date pubdate;
    @Column(name = "rank_score")
    private Long rank_score;
    @Column(name = "review")
    private Integer review;
    @Column(name = "senddate")
    private Date senddate;
    @Column(name = "tag")
    private String tag;
    @Column(name = "title")
    private String title;
    @Column(name = "type")
    private String type;
    @Column(name = "typeid")
    private String typeid;
    @Column(name = "typename")
    private String typename;
    @Column(name = "video_review")
    private Integer video_review;
    @Column(name = "cid")
    private Long cid;

    public Long getAid() {
        return aid;
    }

    public void setAid(Long aid) {
        this.aid = aid;
    }

    public String getArcran() {
        return arcran;
    }

    public void setArcran(String arcran) {
        this.arcran = arcran;
    }

    public String getArcurl() {
        return arcurl;
    }

    public void setArcurl(String arcurl) {
        this.arcurl = arcurl;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Boolean getBadgepay() {
        return badgepay;
    }

    public void setBadgepay(Boolean badgepay) {
        this.badgepay = badgepay;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Integer getFavorites() {
        return favorites;
    }

    public void setFavorites(Integer favorites) {
        this.favorites = favorites;
    }

    public String getHit_columns() {
        return hit_columns;
    }

    public void setHit_columns(String hit_columns) {
        this.hit_columns = hit_columns;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMid() {
        return mid;
    }

    public void setMid(Long mid) {
        this.mid = mid;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public Long getPlay() {
        return play;
    }

    public void setPlay(Long play) {
        this.play = play;
    }

    public Date getPubdate() {
        return pubdate;
    }

    public void setPubdate(Date pubdate) {
        this.pubdate = pubdate;
    }

    public Long getRank_score() {
        return rank_score;
    }

    public void setRank_score(Long rank_score) {
        this.rank_score = rank_score;
    }

    public Integer getReview() {
        return review;
    }

    public void setReview(Integer review) {
        this.review = review;
    }

    public Date getSenddate() {
        return senddate;
    }

    public void setSenddate(Date senddate) {
        this.senddate = senddate;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeid() {
        return typeid;
    }

    public void setTypeid(String typeid) {
        this.typeid = typeid;
    }

    public String getTypename() {
        return typename;
    }

    public void setTypename(String typename) {
        this.typename = typename;
    }

    public Integer getVideo_review() {
        return video_review;
    }

    public void setVideo_review(Integer video_review) {
        this.video_review = video_review;
    }

    public Long getCid() {
        return cid;
    }

    public void setCid(Long cid) {
        this.cid = cid;
    }
}
