package com.stephen.lab.util.paser.zhengce;

import com.stephen.lab.model.crawler.Gopolicy;
import com.stephen.lab.util.StringUtils;
import com.stephen.lab.util.paser.Parser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stephen on 2017/10/29.
 */
public class GopolicyParser implements Parser {
    @Override
    public List parse(String html) {
        if (StringUtils.isNull(html)) {
            return new ArrayList();
        }
        List<Gopolicy> gopolicies = new ArrayList<>();
        Document document = Jsoup.parse(html);
        Elements pTitles = document.select("p[class=title]");
        for (Element pTitle : pTitles) {
            try {
                String url = pTitle.select("a").first().attr("href");
                Gopolicy gopolicy = new Gopolicy();
                gopolicy.setUrl(url);
                gopolicies.add(gopolicy);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return gopolicies;
    }

    @Override
    public Object parseDetail(String html) {
        if (StringUtils.isNull(html)) {
            return null;
        }
        Gopolicy gopolicy = new Gopolicy();
        Document document = Jsoup.parse(html);
        try {
            Element title = document.select("p[class=ztitle]").first();
            gopolicy.setTitle(title.text());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Element div = document.select("div[style]").get(1);
            String text = div.text();
            try {
                Element furl = div.select("a").first();
                String fullTextUrl = furl.attr("href");
                gopolicy.setFull_text_url(fullTextUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
            int absIndex = text.indexOf("[ 摘要 ]");
            if (absIndex != -1) {
                String abs = text.substring(absIndex, text.indexOf("[", absIndex + 1)).replaceAll("\\[ 摘要 \\]", "");
                gopolicy.setAbs(abs);
            }
            int pubDateIndex = text.indexOf("[ 发布日期 ]");
            if (pubDateIndex != -1) {
                String pubDate = text.substring(pubDateIndex, text.indexOf("[ ", pubDateIndex + 1)).replaceAll("\\[ 发布日期 \\]", "");
                gopolicy.setPubDate(pubDate);
            }
            int pubOrgIndex = text.indexOf("[ 发布机构 ]");
            if (pubOrgIndex != -1) {
                String pubOrg = text.substring(pubOrgIndex, text.indexOf("[ ", pubOrgIndex + 1)).replaceAll("\\[ 发布机构 \\]", "");
                gopolicy.setPubOrg(pubOrg);
            }
            int levelIndex = text.indexOf("[ 效力级别 ]");
            if (levelIndex != -1) {
                String level = text.substring(levelIndex, text.indexOf("[ ", levelIndex + 1)).replaceAll("\\[ 效力级别 \\]", "");
                gopolicy.setLevel(level);
            }
            int classifyIndex = text.indexOf("[ 学科分类 ]");
            if (classifyIndex != -1) {
                String classify = text.substring(classifyIndex, text.indexOf("[ ", classifyIndex + 1)).replaceAll("\\[ 学科分类 \\]", "");
                gopolicy.setClassify(classify);
            }
            int keywordIndex = text.indexOf("[ 关键词 ]");
            if (keywordIndex != -1) {
                String keyword = text.substring(keywordIndex, text.indexOf("[ ", keywordIndex + 1)).replaceAll("\\[ 关键词 \\]", "");
                gopolicy.setKeyword(keyword);
            }
            int codeIndex = text.indexOf("[ 发文字号 ]");
            if (codeIndex != -1) {
                String code = text.substring(codeIndex, text.indexOf("[ ", codeIndex + 1)).replaceAll("\\[ 发文字号 \\]", "");
                gopolicy.setCode(code);
            }
            try {
                int timeIndex = text.indexOf("[ 时效性 ]");
                if (timeIndex != -1) {
                    String time = text.substring(timeIndex, text.indexOf("浏览次数")).replaceAll("\\[ 时效性 \\]", "");
                    gopolicy.setTimeliness(time);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            int browserIndex = text.indexOf("浏览次数");
            if (browserIndex != -1) {
                String browser = text.substring(browserIndex, text.indexOf("全文下载")).replaceAll("浏览次数：", "");
                gopolicy.setBrowserNum(browser);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return gopolicy;
    }
}
