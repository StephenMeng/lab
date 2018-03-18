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
public class CnrPaser implements Parser {

    @Override
    public List<ShiJiuDaMessage> parse(String html) {
        List<ShiJiuDaMessage> shiJiuDaMessages = new ArrayList<>();
        Document document = Jsoup.parse(html);
        Elements elements = document.select("li");
        for (Element element : elements) {
            Element title = element.select("a").first();
            String href = title.attr("href");
            Pattern pattern = Pattern.compile("/([0-9]{8})/");
            Matcher matcher = pattern.matcher(href);
            if (matcher.find()) {
                String dateStr = matcher.group();
                dateStr = dateStr.substring(1, dateStr.length() - 1);
                Date date = DateUtils.parseStringToDate(dateStr, "yyyyMMdd");
                if (date.after(DateUtils.parseStringToDate("20171017", "yyyyMMdd"))) {
                    ShiJiuDaMessage shiJiuDaMessage = new ShiJiuDaMessage();
                    shiJiuDaMessage.setHref(href);
                    shiJiuDaMessage.setSourceId(UrlConstant.WAS_CNR_CN_ID);
                    shiJiuDaMessages.add(shiJiuDaMessage);
                }
            }

        }
        return shiJiuDaMessages;
    }

    @Override
    public ShiJiuDaMessage parseDetail(String html) {
        ShiJiuDaMessage result = new ShiJiuDaMessage();
        Document document = Jsoup.parse(html);
        try {
            Element title = document.select("h2").first();
            result.setTitle(title.text());
        } catch (
                Exception e) {
        }
        try {
            Element classify = document.select("p[class=daoHang]").first();
            result.setClassify(classify.text());
        } catch (
                Exception e) {
        }
        try {
            Elements source = document.select("div[class=source]");
            try {
                result.setPubDate(source.get(0).text());
            } catch (Exception e) {
            }
            try {
                result.setSourceOrg(source.get(1).text());
            } catch (Exception e) {
            }
        } catch (Exception e) {
        }
        try {
            Element content = document.select("div[class=Custom_UnionStyle]").first();
            result.setContent(content.text());
        } catch (
                Exception e) {
        }
        try {
            Element editor = document.select("div[class=editor]").first();
            result.setEditor(editor.text());

        } catch (
                Exception e) {
        }
        try {
            Element keyword = document.select("div[class=keywords]").first();
            result.setKeyword(keyword.text());

        } catch (
                Exception e) {
        }
        return result;
    }
}
