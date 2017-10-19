package com.stephen.lab.controller.crawler;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;


public class ShangHaiDataCrawler {

    public static void main(String[] args) {
        new ShangHaiDataCrawler().crawl();
    }

    public void crawl() {
        //页码
        int page = 10;
        //循环查找每页数据
        for (int i = 1; i <= page; i++) {
            //每页的url，爬数据之前要准确的找到获取数据的url
            String url = "http://www.datashanghai.gov.cn/query!" +
                    "queryProduct.action?currentPage=" + i + "&dataField=1 &organId=&envaluationFraction=&dataName=&title=&dataId=&dataArrays=&deptArrays=&envaluationFractionArrays=&searchName=%E8%AF%B7%E8%BE%93%E5%85%A5%E6%95%B0%E6%8D%AE%2F%E5%BA%94%E7%94%A8%2F%E7%A7%BB%E5%8A%A8%E5%BA%94%E7%94%A8%E5%90%8D%E7%A7%B0%E5%85%B3%E9%94%AE%E8%AF%8D...&orderName=updateDate&orders=desc";
            try {
                //获取网页返回的response
                HttpResponse response = httpGet(url, null, null);
                //将response的流对象解析为string对象
                String html = IOUtils.toString(response.getEntity().getContent());
                //将string对象解析为document对象，方便解析网页
                Document document = Jsoup.parse(html);
                //根据所要获取的数据的标签，进行筛选，要符合document 的select语法
                Elements elements = document.select("div[id=content]").first().select("dt");
                for (Element element : elements) {
                    //根据dom标签提取数据，此处提取的是每行记录的href信息
                    String href = element.select("a").first().attr("href");
                }
            } catch (IOException e) {
                //出错信息保存
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

    /**
     * 根据url获取网站返回的数据
     *
     * @param url      请求地址
     * @param map      请求头
     * @param proxyStr 代理IP地址
     * @return
     * @throws IOException
     */
    public static HttpResponse httpGet(String url, Map<String, String> map, String proxyStr) throws IOException {
        HttpClientBuilder clientBuilder = HttpClients.custom().setConnectionManagerShared(true);
        CloseableHttpClient httpClient = clientBuilder.build();
        HttpGet get = new HttpGet(url);
        RequestConfig requestConfig = null;
        if (proxyStr != null) {
            HttpHost proxy = new HttpHost(proxyStr.substring(0, proxyStr.indexOf(":")),
                    Integer.parseInt(proxyStr.substring(proxyStr.indexOf(":") + 1)));
            requestConfig = RequestConfig.custom()
                    .setSocketTimeout(30000).setConnectTimeout(30000)
                    .setProxy(proxy)
                    .setConnectionRequestTimeout(30000).build();
        } else {
            requestConfig = RequestConfig.custom()
                    .setSocketTimeout(20000).setConnectTimeout(20000)
                    .setConnectionRequestTimeout(20000).build();
        }


        if (map != null) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                get.setHeader(entry.getKey(), entry.getValue());
            }
        }
        get.setConfig(requestConfig);
        HttpResponse httpResponse = httpClient.execute(get);
        return httpResponse;
    }
}
