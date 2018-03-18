package com.stephen.lab.controller.crawler;

import com.stephen.lab.constant.crawler.UrlConstant;
import com.stephen.lab.model.crawler.CrawlError;
import com.stephen.lab.model.semantic.Paper;
import com.stephen.lab.service.crawler.CrawlErrorService;
import com.stephen.lab.service.semantic.PaperService;
import com.stephen.lab.util.HttpUtils;
import com.stephen.lab.util.Response;
import com.stephen.lab.util.paser.CSSCIParser;
import com.stephen.lab.util.paser.Parser;
import com.stephen.lab.util.paser.wanfang.WanFangPaser;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RestController
@RequestMapping("cssci")
public class CSSCICrawler {
    @Autowired
    private PaperService paperService;
    @Autowired
    private CrawlErrorService crawlErrorService;
    private static Map<String, String> heaers = null;

    static {
        heaers = new HashMap<>();
        heaers.put("Cookie", "safedog-flow-item=; PHPSESSID=n0qk2hqg55vk65lgm7uhavk8u4");
//        heaers.put("Host", "new.wanfangdata.com.cn");
    }

    @RequestMapping("crawl")
    public Response crawl(@RequestParam("journal") String journal,
                          @RequestParam("pageNum") Integer pageNum,
                          @RequestParam("pageSize") Integer pageSize) throws IOException {
        Paper conditon = new Paper();
        conditon.setSource(UrlConstant.CSSCI_ID);
        List<Paper> papers = paperService.select(conditon);
        List<String> paperUrls = papers.stream().map(Paper::getPaperUrl).collect(Collectors.toList());
        ExecutorService service = Executors.newFixedThreadPool(50);
        for (int i = 1; i <= pageNum; i++) {
            String url =
//                    "http://cssci.nju.edu.cn/control/controllers.php?control=search_base&action=" +
//                            "search_lysy&title=" + journal + "%252B%252B%252B8%252B%252B%252BAND%257C%257C%257C&xkfl1=&wzlx=&" +
//                            "qkname=&jj=&start_year=1998&end_year=2010&nian=&juan=&qi=&xw1=&xw2=&pagesize=50&pagenow=" + i + "&order_type=nian&order_px=DESC&search_tag=1&session_key=975&rand=0.9815842008316413";
                    "http://cssci.nju.edu.cn/control/controllers.php?control=search_base&action=" +
                            "search_lysy&title=" + journal + "%252B%252B%252B8%252B%252B%252BAND%257C%257C%257C&xkfl1=&wzlx=&" +
                            "qkname=&jj=&start_year=2011&end_year=2017&nian=&juan=&qi=&xw1=&xw2=&pagesize=50&pagenow=" + i + "&order_type=nian&order_px=DESC&search_tag=1&session_key=975&rand=0.9815842008316413";

            service.execute(() -> {
                crawUrl(pageSize, paperUrls, url);
            });
//            break;
        }
        return Response.success(true);
    }

    private boolean crawUrl(Integer pageSize, List<String> paperUrls, String url) {
        try {
            HttpResponse response = HttpUtils.httpGet(url, heaers);
            String result = null;
            try {
                result = IOUtils.toString(response.getEntity().getContent());
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<Paper> messages = new CSSCIParser().parse(result);
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
            crawlErrorService.addErrorItem(url, UrlConstant.CSSCI_ID);
        }
    }

    @RequestMapping("crawl_detail")
    public Response crawlDetial() throws IOException {
        Paper condition = new Paper();
        condition.setSource(UrlConstant.CSSCI_ID);
        List<Paper> paperList = paperService.select(condition);
        Parser parser = new CSSCIParser();
        ExecutorService service = Executors.newFixedThreadPool(100);
        for (Paper paper : paperList) {
            if (paper.getTitle() == null) {
                service.execute(() -> {
                    try {
                        String id = paper.getPaperUrl().substring(paper.getPaperUrl().indexOf("id=") + 3);
                        String url = "http://cssci.nju.edu.cn/control/controllers.php?control=search&action=source_id&id=" + id + "&rand=0.4164878038680604";
                        HttpResponse response = HttpUtils.httpGet(url, heaers);
                        String html = IOUtils.toString(response.getEntity().getContent());
                        Paper p = (Paper) parser.parseDetail(html);
                        p.setPaperId(paper.getPaperId());
//                        p.setPaperUrl(url);
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
