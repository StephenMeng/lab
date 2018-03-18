package com.stephen.lab.util.paser;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.stephen.lab.constant.crawler.UrlConstant;
import com.stephen.lab.model.semantic.Paper;
import com.stephen.lab.util.LogRecod;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stephen on 2017/10/29.
 */
public class CSSCIParser implements Parser {
    @Override
    public List parse(String html) {
        List<Paper> papers = new ArrayList<>();
        JSONObject jsonObject = JSONObject.parseObject(html.substring(1));
        JSONArray jsonArray = jsonObject.getJSONArray("contents");
        for (int i = 0; i < jsonArray.size(); i++) {
            try {
                JSONObject object = jsonArray.getJSONObject(i);
                String id = object.getString("id");
                String sno = object.getString("sno");
                String jour = object.getString("qkmc");
                String url = "http://cssci.nju.edu.cn/ly_search_list.html?id=" + sno;
                Paper paper = new Paper();
                paper.setSource(UrlConstant.CSSCI_ID);
                paper.setPaperLinkId(id);
                paper.setJournal(jour);
                paper.setPaperUrl(url);
                papers.add(paper);
                LogRecod.print(paper);
            } catch (Exception e) {
            }
        }
        return papers;
    }

    @Override
    public Object parseDetail(String html) {
        JSONObject jsonObject = JSONObject.parseObject(html);
        Paper paper = new Paper();
        paper.setAuthor(jsonObject.getString("author"));
        paper.setPaperDescription(jsonObject.getString("catation"));
        JSONArray jsonArray = jsonObject.getJSONArray("contents");
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            try {
                String keyword = object.getString("byc");
                paper.setKeyword(keyword.replace("aaa", ""));
            } catch (Exception e) {
            }
            try {
                String year = object.getString("nian");
                paper.setYear(year);
            } catch (Exception e) {
            }
            try {
                String xmlb = object.getString("xmlb");
                paper.setFund(xmlb);
            } catch (Exception e) {
            }
            try {
                String title = object.getString("lypm");
                paper.setTitle(title);
            } catch (Exception e) {
            }
            try {
                String blpm = object.getString("blpm");
                paper.setTitleEn(blpm);
            } catch (Exception e) {
            }
            try {
                String classify = object.getString("xkfl1");
                paper.setClassifyCn1(classify);
            } catch (Exception e) {
            }
            try {
                String classify2 = object.getString("xkfl2");
                paper.setClassifyCn2(classify2);
            } catch (Exception e) {
            }
            break;
        }
        return paper;
    }
}
