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
public class NeiMengGuParser implements Parser {

    @Override
    public List<ShiJiuDaMessage> parse(String html) {
//        LogRecod.print(html);
        List<ShiJiuDaMessage> shiJiuDaMessages = new ArrayList<>();
        Document document = Jsoup.parse(html);
        Elements elements = document.select("table");
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
                    if (date.after(DateUtils.parseStringToDate("2017-10-16", "yyyy-MM-dd"))) {
                        ShiJiuDaMessage shiJiuDaMessage = new ShiJiuDaMessage();
                        shiJiuDaMessage.setHref(href);
                        shiJiuDaMessage.setSourceId(UrlConstant.NEI_MENG_GU_ID);
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
            Element title = document.select("span[class=STYLE5]").first();
            result.setTitle(title.text());
        } catch (
                Exception e) {
        }
        try {
            Element classify = document.select("td[class=STYLE11 STYLE14]").first();
            result.setClassify(classify.text());
        } catch (
                Exception e) {
        }

        try {
            Elements pubDate = document.select("font[color=#000000]");
            result.setPubDate(pubDate.get(0).text());
            result.setSourceOrg(pubDate.get(2).text());
        } catch (Exception e) {
        }
        try {
            Element content = document.select("td[colspan=2]").first();
            result.setContent(content.text());
        } catch (
                Exception e)

        {
        }

        try

        {
            Element editor = document.select("p[class=STYLE15]").first();
            result.setEditor(editor.text());
        } catch (
                Exception e) {
        }
        return result;
    }
}
