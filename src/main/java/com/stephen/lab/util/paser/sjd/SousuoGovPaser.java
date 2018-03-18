package com.stephen.lab.util.paser.sjd;

import com.stephen.lab.constant.crawler.UrlConstant;
import com.stephen.lab.model.crawler.ShiJiuDaMessage;
import com.stephen.lab.util.LogRecod;
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
public class SousuoGovPaser implements Parser {

    @Override
    public List<ShiJiuDaMessage> parse(String html) {
        List<ShiJiuDaMessage> shiJiuDaMessages = new ArrayList<>();
        Document document = Jsoup.parse(html);
        Elements elements = document.select("h3[class=res-title]");
        for (Element element : elements) {
            Element title = element.select("a").first();
            ShiJiuDaMessage shiJiuDaMessage = new ShiJiuDaMessage();
            shiJiuDaMessage.setHref(title.attr("href"));
            shiJiuDaMessage.setSourceId(UrlConstant.SOUSUO_GOV_CN_ID);
            LogRecod.print(shiJiuDaMessage);
            shiJiuDaMessages.add(shiJiuDaMessage);
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
            Element classify = document.select("div[class=BreadcrumbNav]").first();
            result.setClassify(classify.text());
        } catch (
                Exception e) {
        }
        try {
            Element pubDate = document.select("div[class=pages-date]").first();
            result.setPubDate(pubDate.text());
            try {
                Element sourceOrg = pubDate.select("span[class=font]").first();
                result.setSourceOrg(sourceOrg.text());
            } catch (Exception e) {
            }
        } catch (Exception e) {
        }
        try {
            Element content = document.select("div[class=pages_content]").first();
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
        return result;
    }
}
