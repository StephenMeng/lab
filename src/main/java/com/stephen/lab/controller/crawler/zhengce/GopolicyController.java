package com.stephen.lab.controller.crawler.zhengce;

import com.github.pagehelper.PageHelper;
import com.stephen.lab.constant.crawler.ErrorConstant;
import com.stephen.lab.constant.crawler.UrlConstant;
import com.stephen.lab.constant.semantic.ResultEnum;
import com.stephen.lab.model.crawler.CrawlError;
import com.stephen.lab.model.crawler.Gopolicy;
import com.stephen.lab.service.crawler.CrawlErrorService;
import com.stephen.lab.service.crawler.GopolicyService;
import com.stephen.lab.util.HttpUtils;
import com.stephen.lab.util.LogRecod;
import com.stephen.lab.util.Response;
import com.stephen.lab.util.paser.zhengce.GopolicyParser;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequestMapping("crawler/gopolicy")
@RestController
public class GopolicyController {
    @Autowired
    private GopolicyService gopolicyService;
    @Autowired
    private CrawlErrorService crawlErrorService;

    private GopolicyParser gopolicyParser = new GopolicyParser();

    @RequestMapping("crawlUrl")
    public Response crawlUrl() {

        int pageNum = 10092;
        ExecutorService service = Executors.newFixedThreadPool(50);
        for (int i = 1; i <= pageNum; i++) {
            String url = "http://gopolicy.las.ac.cn/service/prsearch?para=&q=&px=oaviewcount&order=&currentPage=" + i;
            service.execute(() -> {
                crawlOne(url);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        return Response.success(true);
    }

    public Response crawlOne(String url) {
        Response result = Response.success(true);
        int pageSize = 10;
        String html = null;
        try {
            HttpResponse response = HttpUtils.httpGet(url);
            html = IOUtils.toString(response.getEntity().getContent());
        } catch (Exception e) {
            e.printStackTrace();
            result = Response.error(ResultEnum.FAIL_PARAM_WRONG);
        }
        List<Gopolicy> gopolicies = gopolicyParser.parse(html);
        if (gopolicies.size() < pageSize) {
            CrawlError conditon = new CrawlError();
            conditon.setErrorHref(url);
            List<CrawlError> errors = crawlErrorService.select(conditon);
            if (errors == null || errors.size() == 0) {
                crawlErrorService.addErrorItem(url, UrlConstant.CSSCI_ID);
                result = Response.error(ResultEnum.FAIL_PARAM_WRONG);
            }
        }
        gopolicies.forEach(gopolicyService::insert);
        return result;

    }

    @RequestMapping("crawl_detail")
    public Response crawlDetial() throws IOException {
        List<Gopolicy> gopolicies = gopolicyService.selectNullTitle();
        LogRecod.print(gopolicies.size());
        ExecutorService service = Executors.newFixedThreadPool(100);
        for (Gopolicy gopolicy : gopolicies) {
            service.execute(() -> {
                try {
                    String url = gopolicy.getUrl();
                    HttpResponse response = HttpUtils.httpGet(url);
                    String html = IOUtils.toString(response.getEntity().getContent());
                    Gopolicy p = (Gopolicy) gopolicyParser.parseDetail(html);
                    p.setUrl(gopolicy.getUrl());
                    gopolicyService.updateSelective(p);
                    if (p.getFull_text_url() != null) {
                        String fullTextUrl = p.getFull_text_url().replace("workdown", "readhtml");
                        updateContent(gopolicy, fullTextUrl);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

        }
        return Response.success(gopolicies.size());
    }

    @RequestMapping("crawl_content")
    public Response crawlContent() throws IOException {
        List<Gopolicy> gopolicies = gopolicyService.selectNullFullText();
        ExecutorService service = Executors.newFixedThreadPool(100);
        for (Gopolicy gopolicy : gopolicies) {
            if (gopolicy.getFull_text_url() != null) {
                service.execute(() -> {
                    try {
                        String url = gopolicy.getFull_text_url();
                        LogRecod.print(url);
                        url = url.replace("workdown", "readhtml");
                        updateContent(gopolicy, url);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }

        }
        return Response.success(true);
    }

    private void updateContent(Gopolicy gopolicy, String fullTextUrl) throws IOException {
        HttpResponse response = HttpUtils.httpGet(fullTextUrl);
        String html = IOUtils.toString(response.getEntity().getContent());
        Gopolicy g = new Gopolicy();
        g.setUrl(gopolicy.getUrl());
        g.setContentHtml(html);
        g.setContent(Jsoup.parse(html).text());
        gopolicyService.updateSelective(g);
    }

    @RequestMapping("crawl_error")
    public Response crawl() throws IOException {
        CrawlError conditon = new CrawlError();
        conditon.setSourceId(UrlConstant.CSSCI_ID);
        conditon.setStatus(ErrorConstant.ERROR);
        List<CrawlError> errors = crawlErrorService.select(conditon);
        ExecutorService service = Executors.newFixedThreadPool(20);
        errors.forEach(error -> service.execute(() -> {
            Response res = crawlOne(error.getErrorHref());
            if (res.getCode() == 200) {
                error.setStatus(ErrorConstant.REPAIRED);
                crawlErrorService.updateSelective(error);
            }
        }));
        return Response.success(true);
    }
}
