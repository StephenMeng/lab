package com.stephen.lab.controller.crawler;

import com.stephen.lab.model.crawler.ShangHaiData;
import com.stephen.lab.service.crawler.ShangHaiDataService;
import com.stephen.lab.util.HttpUtils;
import com.stephen.lab.util.LogRecod;
import com.stephen.lab.util.Response;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("shanghai_data")
public class ShangHaiDataController {
    @Autowired
    private ShangHaiDataService shangHaiDataService;

    @RequestMapping("getId")
    public Response getId() {
        Map<Integer, Integer> count = new HashMap<>();
        count.put(1, 24);
        count.put(2, 5);
        count.put(3, 9);
        count.put(4, 10);
        count.put(5, 5);
        count.put(6, 3);
        count.put(7, 5);
        count.put(8, 10);
        count.put(9, 7);
        count.put(10, 5);
        count.put(11, 10);
        count.put(12, 2);
        for (Map.Entry<Integer, Integer> m : count.entrySet()) {
            Integer filed = m.getKey();
            Integer page = m.getValue();
            for (int i = 1; i <= page; i++) {

                String url = "http://www.datashanghai.gov.cn/query!" +
                        "queryProduct.action?currentPage=" + i + "&dataField=" + filed +
                        "&organId=&envaluationFraction=&dataName=&title=&dataId=&dataArrays=&deptArrays=&envaluationFractionArrays=&searchName=%E8%AF%B7%E8%BE%93%E5%85%A5%E6%95%B0%E6%8D%AE%2F%E5%BA%94%E7%94%A8%2F%E7%A7%BB%E5%8A%A8%E5%BA%94%E7%94%A8%E5%90%8D%E7%A7%B0%E5%85%B3%E9%94%AE%E8%AF%8D...&orderName=updateDate&orders=desc";
                try {
                    HttpResponse response = HttpUtils.httpGet(url);
                    String html = IOUtils.toString(response.getEntity().getContent());
                    Document document = Jsoup.parse(html);
                    Elements elements = document.select("div[id=content]").first().select("dt");
                    for (Element element : elements) {
                        String href = element.select("a").first().attr("href");
                        ShangHaiData data = new ShangHaiData();
                        data.setDataId(href);
//                        data.setDataId(href.substring(href.lastIndexOf("=") + 1));
                        data.setHref(href);
                        try {
                            shangHaiDataService.add(data);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    try {
                        FileWriter fileWriter = new FileWriter(new File("c:/users/stephen/desktop/sh-data-error.txt"), true);
                        fileWriter.write(url + "\r\n");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    e.printStackTrace();
                }
            }
        }
        return Response.success(true);
    }

    @RequestMapping("getDetail")
    public Response getDetail() {
        List<ShangHaiData> dataList = shangHaiDataService.selectAll();
        for (ShangHaiData shangHaiData : dataList) {
            if (shangHaiData.getTitle() == null) {
                String url = "http://www.datashanghai.gov.cn/" + shangHaiData.getHref();
                try {
                    ShangHaiData data = new ShangHaiData();
                    data.setDataId(shangHaiData.getDataId());
                    HttpResponse response = HttpUtils.httpGet(url);
                    String html = IOUtils.toString(response.getEntity().getContent());
                    Document document = Jsoup.parse(html);
                    String title = document.select("h2[class=dataTitle]").text();
                    data.setTitle(title.substring(0, title.indexOf(" ")));
                    String star = document.select("em[class=star2]").text();
                    data.setStar(star);
                    Element table = document.select("table[class=data]").first();
                    Elements trs = table.select("tr");
                    for (Element element : trs) {
                        if (element.html().contains("下载次数")) {
                            String result = element.select("td").text();
                            data.setViewNum(Integer.parseInt(result.substring(0, result.indexOf("/")).trim()));
                            data.setDownloadNum(Integer.parseInt(result.substring(result.indexOf("/") + 1).trim()));
                        }
                        if (element.html().contains("摘要")) {
                            String result = element.select("td").text();
                            data.setDataAbstract(result);
                        }
                        if (element.html().contains("应用场景")) {
                            String result = element.select("td").text();
                            data.setScenarios(result);
                        }
                        if (element.html().contains("数据标签")) {
                            String result = element.select("td").text();
                            data.setTag(result);
                        }
                        if (element.html().contains("关键字")) {
                            String result = element.select("td").text();
                            data.setKeywords(result);
                        }
                        if (element.html().contains("数据领域")) {
                            String result = element.select("td").text();
                            data.setArea(result);
                        }
                        if (element.html().contains("国家主题分类")) {
                            String result = element.select("td").text();
                            data.setClassifyCountry(result);
                        }
                        if (element.html().contains("部门主题分类")) {
                            String result = element.select("td").text();
                            data.setClassifyDept(result);
                        }
                        if (element.html().contains("公开属性")) {
                            String result = element.select("td").text();
                            data.setPublicType(result);
                        }
                        if (element.html().contains("首次发布")) {
                            String result = element.select("td").text();
                            data.setPubDate(result);
                        }
                        if (element.html().contains("更新日期")) {
                            String result = element.select("td").text();
                            data.setUpdateDate(result);
                        }
                        if (element.html().contains("数据提供方")) {
                            String result = element.select("td").text();
                            data.setSource(result);
                        }
                    }
                    LogRecod.print(data);
                    shangHaiDataService.updateSelective(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return Response.success(true);

    }
}
