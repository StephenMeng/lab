package com.stephen.lab.util.paser.sjd;

import com.stephen.lab.constant.crawler.UrlConstant;
import com.stephen.lab.model.crawler.ShiJiuDaMessage;
import com.stephen.lab.util.DateUtils;
import com.stephen.lab.util.LogRecod;
import com.stephen.lab.util.paser.Parser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sun.rmi.runtime.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by stephen on 2017/10/22.
 */
public class QinghaiParser implements Parser {

    @Override
    public List<ShiJiuDaMessage> parse(String html) {
//        LogRecod.print(html);
        List<ShiJiuDaMessage> shiJiuDaMessages = new ArrayList<>();
        Document document = Jsoup.parse(html);
        Elements elements = document.select("td[class=searchTitle]");
        for (Element element : elements) {
            try {
                Element title = element.select("a").first();
                String href = title.attr("href");
                Pattern pattern = Pattern.compile("/([0-9]{4})/([0-9]{2})/([0-9]{2})/");
                Matcher matcher = pattern.matcher(href);
                if (matcher.find()) {
                    String dateStr = matcher.group();
                    dateStr = dateStr.substring(1, dateStr.length() - 1);
                    dateStr = dateStr.replaceAll("/", "-");
                    Date date = DateUtils.parseStringToDate(dateStr, "yyyy-MM-dd");
                    if (date.after(DateUtils.parseStringToDate("2017-10-17", "yyyy-MM-dd"))) {
                        ShiJiuDaMessage shiJiuDaMessage = new ShiJiuDaMessage();
                        shiJiuDaMessage.setHref(href);
                        shiJiuDaMessage.setSourceId(UrlConstant.SOU_QHNEWS_COM_ID);
                        shiJiuDaMessages.add(shiJiuDaMessage);
                    }
                }
            } catch (Exception e) {
            }
        }
        return shiJiuDaMessages;
    }

    @Override
    public ShiJiuDaMessage parseDetail(String html) {
        ShiJiuDaMessage result = new ShiJiuDaMessage();
        Document document = Jsoup.parse(html);
        try {
            Element title = document.select("h1[class=details_title]").first();
            result.setTitle(title.text());
        } catch (
                Exception e) {
        }
        try {
            Element classify = document.select("div[class=pt20]").first();
            result.setClassify(classify.text());
        } catch (
                Exception e) {
        }

        try {
            Element pubDate = document.select("span[id=ArticleCreatedAt]").first();
            result.setPubDate(pubDate.text());
        } catch (Exception e) {
            try {
                Element pubDate = document.select("div[class=abstract ta_c]").first();
                String date = pubDate.text();
                date = date.substring(date.indexOf("发布时间"), date.indexOf("编辑"));
                result.setPubDate(date);
            } catch (Exception e2) {
            }
        }
        try {
            Element source = document.select("span[id=ArticleSource]").first();

            result.setSourceOrg(source.text());
        } catch (Exception e) {
            try {
                Element pubDate = document.select("div[class=abstract ta_c]").first();
                String date = pubDate.text();
                date = date.substring(date.indexOf("来源"), date.indexOf("作者"));
                result.setSourceOrg(date);
            }catch (Exception e2){}
        }
        try {
            Element content = document.select("div[class=clearfix details_content hui13]").first();
            result.setContent(content.text());
        } catch (
                Exception e)

        {
        }

        try

        {
            Element editor = document.select("span[id=ArticleAuthor]").first();
            result.setEditor(editor.text());
        } catch (
                Exception e) {
            try {
                Element pubDate = document.select("div[class=abstract ta_c]").first();
                String date = pubDate.text();
                date = date.substring(date.indexOf("编辑"));
                result.setEditor(date);
            } catch (Exception e2) {
            }
        }
        return result;
    }
}
