package com.stephen.lab.util.paser;

import com.stephen.lab.model.crawler.Weibo;
import com.stephen.lab.util.CodeUtils;
import com.stephen.lab.util.LogRecod;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by stephen on 2017/10/29.
 */
public class WeiboSearchParser implements Parser {
    @Override
    public List parse(String html) {
        List<Weibo> weiboList = new ArrayList<>();
        Document document = Jsoup.parse(html);
        Elements detail = document.select("div[tbinfo]");
        for (Element element : detail) {
            Element eLike = element.select("div[class=feed_action clearfix]").first();
            Element screenBox = element.select("div[class=screen_box]").first();
            Weibo weibo = new Weibo();
            try {
                Element user = element.select("div[class=feed_content wbcon]").first();
                String userName = user.select("a").first().text();
                weibo.setUserName(userName.substring(3, userName.length() - 1));
                weibo.setUserUrl(user.select("a").first().attr("href"));
                weibo.setVIcon(user.select("a").get(1).attr("href"));
                weibo.setVLevel(user.select("a").get(2).html());
            } catch (Exception e) {
            }
            try {
                String li = screenBox.select("li").first().html();
                weibo.setMid(li.substring(li.indexOf("mid"), li.indexOf("src")));
            } catch (Exception e) {
            }
            try {
                Element face = element.select("div[class=face]").first();
                weibo.setUserAvatarUrl(face.select("img").attr("src"));
            } catch (Exception e) {
            }
            try {
                Element pubDate = element.select("div[class=feed_from W_textb]").first();

                try {
                    weibo.setPubDate(pubDate.select("a").first().text());
                } catch (Exception e) {
                }
                try {
                    weibo.setUrl(pubDate.select("a").first().attr("href"));
                } catch (Exception e) {
                }
                try {
                    Element source = pubDate.select("a").get(1);
                    weibo.setSource(source.text());
                } catch (Exception e) {
                }
            } catch (Exception e) {
            }
            try {
                Element content = element.select("p[class=comment_txt]").first();
                weibo.setContent(content.text());
            } catch (Exception e) {
            }
            try {
                Elements pictures = element.select("div[class=media_box]").select("li");
                StringBuilder sb = new StringBuilder();
                pictures.forEach(e -> sb.append(e.select("img").first().attr("src") + ";"));
                weibo.setPictureUrls(sb.toString());
            } catch (Exception e) {
            }
            try {
                Elements interactData = eLike.select("li");
                Element share = interactData.get(1);
                weibo.setShareNum(share.text());
                Element comment = interactData.get(2);
                weibo.setCommentNum(comment.text());

                Element like = interactData.get(3);
                weibo.setLikeNum(like.text());
            } catch (Exception e) {
            }
            weibo.setCreateDate(new Date());
            if (weibo.getUrl() != null) {
                weiboList.add(weibo);
            }
        }
        return weiboList;
    }

    @Override
    public Object parseDetail(String html) {
        return null;
    }
}
