package com.stephen.lab.controller.crawler.wanfang;

import com.stephen.lab.constant.crawler.ErrorConstant;
import com.stephen.lab.constant.crawler.UrlConstant;
import com.stephen.lab.model.crawler.CrawlError;
import com.stephen.lab.model.crawler.ShiJiuDaMessage;
import com.stephen.lab.model.semantic.Paper;
import com.stephen.lab.service.crawler.CrawlErrorService;
import com.stephen.lab.service.crawler.NstrsService;
import com.stephen.lab.service.semantic.PaperService;
import com.stephen.lab.util.HttpUtils;
import com.stephen.lab.util.LogRecod;
import com.stephen.lab.util.Response;
import com.stephen.lab.util.paser.Parser;
import com.stephen.lab.util.paser.sjd.CnrPaser;
import com.stephen.lab.util.paser.wanfang.WanFangPaser;
import com.sun.org.apache.regexp.internal.RE;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RestController
@RequestMapping("wanfang")
public class WanFangController {
    @Autowired
    private NstrsService nstrsService;

    public static String api = "";

    public static String proxy = null;
    private static Boolean running = true;
    @Autowired
    private PaperService paperService;
    @Autowired
    private CrawlErrorService crawlErrorService;

    @RequestMapping("crawl")
    public Response crawl(@RequestParam("journal") String journal,
                          @RequestParam("pageNum") Integer pageNum,
                          @RequestParam("pageSize") Integer pageSize) throws IOException {
        Paper conditon = new Paper();
        conditon.setSource(UrlConstant.WAN_FAGN_ID);
        List<Paper> papers = paperService.select(conditon);
        List<String> paperUrls = papers.stream().map(Paper::getPaperUrl).collect(Collectors.toList());
        ExecutorService service = Executors.newFixedThreadPool(100);
        for (int i = 1; i <= pageNum; i++) {
            String url = "http://new.wanfangdata.com.cn/search/searchList.do?searchType=perio&pageSize=" + pageSize
                    + "&searchWord=%20%E5%88%8A%E5%90%8D:" + journal
                    + "&showType=detail&order=correlation&isHit=&isHitUnit=" +
                    "&firstAuthor=false&rangeParame=all&page=" + i;
            service.execute(() -> {
                crawUrl(pageSize, paperUrls, url);
            });
        }
        return Response.success(true);
    }

    private boolean crawUrl(Integer pageSize, List<String> paperUrls, String url) {
        try {
            HttpResponse response = HttpUtils.httpGet(url);
            String result = null;
            try {
                result = IOUtils.toString(response.getEntity().getContent());
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<Paper> messages = new WanFangPaser().parse(result);
            if (messages.size() < pageSize) {
                addErrorIte(url);
            }
            messages.forEach(paper -> {
                if (!paperUrls.contains(paper.getPaperUrl())) {
                    paperService.addPaper(paper);
                }
            });
        } catch (Exception e) {
            addErrorIte(url);
            return false;
        }
        return true;
    }

    private void addErrorIte(String url) {
        CrawlError conditon = new CrawlError();
        conditon.setErrorHref(url);
        List<CrawlError> errors = crawlErrorService.select(conditon);
        if (errors == null || errors.size() == 0) {
            crawlErrorService.addErrorItem(url, UrlConstant.WAN_FAGN_ID);
        }
    }

    @RequestMapping("crawl_error")
    public Response crawl() throws IOException {
        CrawlError conditon = new CrawlError();
        conditon.setSourceId(UrlConstant.WAN_FAGN_ID);
        conditon.setStatus(ErrorConstant.ERROR);
        List<CrawlError> errors = crawlErrorService.select(conditon);
        Paper paperConditon = new Paper();
        paperConditon.setSource(UrlConstant.WAN_FAGN_ID);
        List<Paper> papers = paperService.select(paperConditon);
        List<String> paperUrls = papers.stream().map(Paper::getPaperUrl).collect(Collectors.toList());
        ExecutorService service = Executors.newFixedThreadPool(20);
        errors.forEach(error -> {
            service.execute(() -> {
                String url = error.getErrorHref();
                if (crawUrl(50, paperUrls, url)) {
                    error.setStatus(ErrorConstant.REPAIRED);
                    crawlErrorService.updateSelective(error);
                }
            });
        });
        return Response.success(true);
    }

    @RequestMapping("crawl_detail")
    public Response crawlDetial() throws IOException {
        Paper condition = new Paper();
        condition.setSource(UrlConstant.WAN_FAGN_ID);
        List<Paper> paperList = paperService.select(condition);
        Parser parser = new WanFangPaser();
        ExecutorService service = Executors.newFixedThreadPool(100);
        for (Paper paper : paperList) {
            if (paper.getAuthor() == null) {
                service.execute(() -> {
                    try {
                        HttpResponse response = HttpUtils.httpGet(paper.getPaperUrl());
                        String html = IOUtils.toString(response.getEntity().getContent());
                        Paper p = (Paper) parser.parseDetail(html);
                        p.setPaperId(paper.getPaperId());
                        paperService.updateSelective(p);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
        return Response.success(true);
    }
}