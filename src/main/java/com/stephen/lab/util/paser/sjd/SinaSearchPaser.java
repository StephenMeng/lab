package com.stephen.lab.util.paser.sjd;

import com.stephen.lab.constant.crawler.UrlConstant;
import com.stephen.lab.model.crawler.ShiJiuDaMessage;
import com.stephen.lab.model.crawler.SinaSearch;
import com.stephen.lab.util.LogRecod;
import com.stephen.lab.util.paser.Parser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by stephen on 2017/10/22.
 */
public class SinaSearchPaser implements Parser {

    @Override
    public List<ShiJiuDaMessage> parse(String html) {
        List<ShiJiuDaMessage> shiJiuDaMessages = new ArrayList<>();
        Document document = Jsoup.parse(html);
        Elements elements = document.select("div[class=box-result clearfix]");
        for (Element element : elements) {
            Element title = element.select("a").first();
//            Element pubDate = element.select("span[class=fgray_time]").first();
//            Element abstractData = element.select("p[class=content]").first();
            ShiJiuDaMessage shiJiuDaMessage = new ShiJiuDaMessage();
            shiJiuDaMessage.setHref(title.attr("href"));
            shiJiuDaMessage.setSourceId(UrlConstant.SEARCH_SINA_COM_ID);
            LogRecod.print(shiJiuDaMessage.getHref());
            shiJiuDaMessages.add(shiJiuDaMessage);
        }
        return shiJiuDaMessages;
    }

    @Override
    public ShiJiuDaMessage parseDetail(String html) {
        ShiJiuDaMessage result = new ShiJiuDaMessage();
        Document document = Jsoup.parse(html);
        try {
            Element title = document.select("h1[id=artibodyTitle]").first();
            result.setTitle(title.text());
        } catch (
                Exception e) {
        }
        try {
            Element pageInfo = document.select("div[class=page-info]").first();
            try {
                Element commentCount = pageInfo.select("span[id=commentCount1]").first();
                result.setCommentCount(commentCount.text());
            } catch (Exception e) {
            }
            try {
                Element pubDate = pageInfo.select("span[class=time-source]").first();
                result.setPubDate(pubDate.text());
            } catch (Exception e) {
            }
            try {
                Element sourceOrg = pageInfo.select("span[data-sudaclick=media_name]").first();
                result.setSourceOrg(sourceOrg.text());
            } catch (Exception e) {
            }
        } catch (Exception ee) {
        }
        try {
            Element content = document.select("div[class=article article_16]").first();
            result.setContent(content.text());
        } catch (
                Exception e) {
        }
        try {
            Element editor = document.select("p[class=article-editor]").first();
            result.setEditor(editor.text());

        } catch (
                Exception e) {
        }
        try {
            Element keyword = document.select("div[class=article-keywords]").first();
            result.setKeyword(keyword.text());

        } catch (
                Exception e) {
        }
        return result;
    }
}
