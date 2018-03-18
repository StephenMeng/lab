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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by stephen on 2017/10/22.
 */
public class ChinaDailyParser implements Parser {

    @Override
    public List<ShiJiuDaMessage> parse(String html) {
        List<ShiJiuDaMessage> shiJiuDaMessages = new ArrayList<>();
        Document document = Jsoup.parse(html);
        Elements elements = document.select("h3");
        for (Element element : elements) {
            Element title = element.select("a").first();
            String href = title.attr("href");
            ShiJiuDaMessage shiJiuDaMessage = new ShiJiuDaMessage();
            shiJiuDaMessage.setHref(href);
            shiJiuDaMessage.setSourceId(UrlConstant.CHINA_DAILY_COM_ID);
            shiJiuDaMessages.add(shiJiuDaMessage);
        }
        return shiJiuDaMessages;
    }

    @Override
    public ShiJiuDaMessage parseDetail(String html) {
        ShiJiuDaMessage result = new ShiJiuDaMessage();
        Document document = Jsoup.parse(html);
        try {
            Element title = document.select("h1[class=dabiaoti]").first();
            result.setTitle(title.text());
        } catch (
                Exception e) {
        }
        try {
            Element classify = document.select("div[class=da-bre]").first();
            result.setClassify(classify.text());
        } catch (
                Exception e) {
        }

        try {
            Element pubDate = document.select("div[id=pubtime]").first();

            result.setPubDate(pubDate.text());
        } catch (Exception e) {
        }
        try {
            Element source = document.select("div[id=source]").first();

            result.setSourceOrg(source.text());
        } catch (Exception e) {
        }
        try {
            Element content = document.select("div[id=Content]").first();
            result.setContent(content.text());
        } catch (
                Exception e)

        {
        }
        try

        {
            Element keyword = document.select("div[class=fenx-bq]").first();
            result.setKeyword(keyword.text());

        } catch (
                Exception e)

        {
        }
        try

        {
            Element keyword = document.select("div[class=keywords]").first();
            result.setKeyword(keyword.text());

        } catch (
                Exception e)

        {
        }
        return result;
    }
}
