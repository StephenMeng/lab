package com.stephen.lab.util.paser.sjd;

import com.stephen.lab.constant.crawler.UrlConstant;
import com.stephen.lab.model.crawler.ShiJiuDaMessage;
import com.stephen.lab.util.paser.Parser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by stephen on 2017/10/22.
 */
public class XinJiangGuParser implements Parser {

    @Override
    public List<ShiJiuDaMessage> parse(String html) {
//        LogRecod.print(html);
        List<ShiJiuDaMessage> shiJiuDaMessages = new ArrayList<>();
        Document document = Jsoup.parse(html);
        Elements elements = document.select("li");
        for (Element element : elements) {
            try {
                Element title = element.select("a").first();
                String href = title.attr("href");
                if (title.text().contains("十九大")) {
                    ShiJiuDaMessage shiJiuDaMessage = new ShiJiuDaMessage();
                    shiJiuDaMessage.setHref(href);
                    shiJiuDaMessage.setSourceId(UrlConstant.TS_CN_ID);
                    shiJiuDaMessages.add(shiJiuDaMessage);
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
            Element title = document.select("h1").first();
            result.setTitle(title.text());
        } catch (
                Exception e) {
        }
        try {
            Element classify = document.select("div[class=yinti]").first();
            result.setClassify(classify.text());
        } catch (
                Exception e) {
        }

        try {
            Element pubDate = document.select("p[class=active-info2]").first();
            String source=pubDate.text();
            result.setPubDate(source.substring(0,source.indexOf("来源")));
            result.setSourceOrg(source.substring(source.indexOf("来源")));

        } catch (Exception e) {
        }
        try {
            Element content = document.select("div[class=hy-active]").first();
            result.setContent(content.text());
        } catch (
                Exception e)

        {
        }

        try

        {
            Element editor = document.select("div[class=zebian]").first();
            result.setEditor(editor.text());
        } catch (
                Exception e) {
        }
        return result;
    }
}
