package com.stephen.lab.util.paser.wanfang;

import com.stephen.lab.constant.DataSourceType;
import com.stephen.lab.constant.crawler.UrlConstant;
import com.stephen.lab.model.crawler.ShiJiuDaMessage;
import com.stephen.lab.model.semantic.Paper;
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
public class WanFangPaser implements Parser {

    @Override
    public List<Paper> parse(String html) {
        List<Paper> papers = new ArrayList<>();
        Document document = Jsoup.parse(html);
        Elements elements = document.select("div[class=ResultCont]");
        elements.forEach(element -> {
            Paper paper = new Paper();
            Element title = element.select("a[target=_blank]").first();
            Element href = element.select("i[title=WFMetrics]").first();
            paper.setTitle(title.html());
            String pagperUrl = getPaperUrl(href);
            paper.setPaperUrl(pagperUrl);
            paper.setSource(UrlConstant.WAN_FAGN_ID);
            papers.add(paper);
        });
        return papers;
    }

    private String getPaperUrl(Element href) {
        String showbox = href.attr("onclick");
        String id = showbox.substring(showbox.indexOf(",'") + 2, showbox.indexOf("',"));
        String perio = showbox.substring(showbox.lastIndexOf(",'") + 2, showbox.indexOf("')"));
        String url = "http://new.wanfangdata.com.cn/details/detail.do?_type=" + perio + "&id=" + id;
        return url;
    }

    @Override
    public Paper parseDetail(String html) {
        Paper paper = new Paper();
        Document document = Jsoup.parse(html);
        Elements detail = document.select("div[class=info_right]");
        try {
            Element pAbstract = document.select("div[class=abstract]").first().select("textarea").first();
            paper.setSummary(pAbstract.text().replace("摘要：", ""));
        } catch (Exception e) {
        }
        try {
            Element keyword = detail.get(0);
            paper.setKeyword(keyword.text());
        } catch (
                Exception e) {
        }
        try {
            Element autor = detail.get(2);
            paper.setAuthor(autor.text());
        } catch (
                Exception e) {
        }

        try {
            Element autorOrg = detail.get(3);
            paper.setOrgan(autorOrg.text());
        } catch (
                Exception e) {
        }
        try {
            Element journal = detail.get(4);
            paper.setJournal(journal.text());
        } catch (
                Exception e) {
        }
        try {
            Element year = detail.get(5);
            paper.setYear(year.text());
        } catch (
                Exception e) {
        }
        try {
            Element fund = document.select("div[class=info_right author]").get(3);
            paper.setFund(fund.text());
        } catch (
                Exception e) {
        }
        try {
            Element doi = document.select("div[class=info_right author]").first();
            paper.setPaperLinkId(doi.text());
        } catch (
                Exception e) {
        }
        return paper;
    }
}
