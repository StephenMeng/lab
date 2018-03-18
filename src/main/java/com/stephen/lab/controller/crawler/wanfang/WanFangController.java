package com.stephen.lab.controller.crawler.wanfang;

import com.stephen.lab.constant.crawler.ErrorConstant;
import com.stephen.lab.constant.crawler.UrlConstant;
import com.stephen.lab.model.crawler.CrawlError;
import com.stephen.lab.model.semantic.Paper;
import com.stephen.lab.service.crawler.CrawlErrorService;
import com.stephen.lab.service.crawler.NstrsService;
import com.stephen.lab.service.semantic.PaperService;
import com.stephen.lab.util.HttpUtils;
import com.stephen.lab.util.LogRecod;
import com.stephen.lab.util.Response;
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
@RequestMapping("wanfang")
public class WanFangController {
    @Autowired
    private NstrsService nstrsService;

    public static String api = "";

    public static String proxy = null;
    private static Boolean running = true;
    private static Map<String, String> heaers = null;
    static {
        heaers = new HashMap<>();
        heaers.put("Cookie",
                "Hm_lvt_fc6472165a980e1a4d297a9cd17bcadd=1512293982,1512294569,1512294569,1512295072; zh_choose=n; Hm_lvt_838fbc4154ad87515435bf1e10023fab=1514783830; CASTGC=TGT-463027-9InI7y3YAXCQQOlFaJBHCvu7LXAuYL2MdlMexE2Egwmzrxa75i-my.wanfangdata.com.cn; SEARCHHISTORY_0=%5B%7B%22addEndTIme%22%3A%22%22%2C%22addStartTime%22%3A%22%22%2C%22count%22%3A9654%2C%22date%22%3A%222018-01-01%22%2C%22id%22%3A1514792436038%2C%22strategy%22%3A%22+%E5%88%8A%E5%90%8D%3A%E5%A4%A7%E5%AD%A6%E5%9B%BE%E4%B9%A6%E9%A6%86%E5%AD%A6%E6%8A%A5%22%2C%22type%22%3A%22perio%22%2C%22typeName%22%3A%22%E6%9C%9F%E5%88%8A%22%2C%22url%22%3A%22%2Fsearch%2FsearchList.do%3FsearchType%3Dperio%26pageSize%3D20%26page%3D2%26searchWord%3D%2520%25E5%2588%258A%25E5%2590%258D%3A%25E5%25A4%25A7%25E5%25AD%25A6%25E5%259B%25BE%25E4%25B9%25A6%25E9%25A6%2586%25E5%25AD%25A6%25E6%258A%25A5%26order%3Dcorrelation%26showType%3Ddetail%26isCheck%3Dcheck%26isHit%3D%26isHitUnit%3D%26firstAuthor%3Dfalse%26rangeParame%3Dall%22%7D%2C%7B%22addEndTIme%22%3A%22%22%2C%22addStartTime%22%3A%22%22%2C%22count%22%3A9654%2C%22date%22%3A%222018-01-01%22%2C%22id%22%3A1514792422021%2C%22strategy%22%3A%22+%E5%88%8A%E5%90%8D%3A%E5%A4%A7%E5%AD%A6%E5%9B%BE%E4%B9%A6%E9%A6%86%E5%AD%A6%E6%8A%A5%22%2C%22type%22%3A%22perio%22%2C%22typeName%22%3A%22%E6%9C%9F%E5%88%8A%22%2C%22url%22%3A%22%2Fsearch%2FsearchList.do%3FsearchType%3Dperio%26showType%3Ddetail%26searchWord%3D%2B%25E5%2588%258A%25E5%2590%258D%253A%25E5%25A4%25A7%25E5%25AD%25A6%25E5%259B%25BE%25E4%25B9%25A6%25E9%25A6%2586%25E5%25AD%25A6%25E6%258A%25A5%26isTriggerTag%3D%22%7D%2C%7B%22addEndTIme%22%3A%22%22%2C%22addStartTime%22%3A%22%22%2C%22count%22%3A9654%2C%22date%22%3A%222018-01-01%22%2C%22id%22%3A1514786723862%2C%22strategy%22%3A%22+%E5%88%8A%E5%90%8D%3A%E5%A4%A7%E5%AD%A6%E5%9B%BE%E4%B9%A6%E9%A6%86%E5%AD%A6%E6%8A%A5%22%2C%22type%22%3A%22perio%22%2C%22typeName%22%3A%22%E6%9C%9F%E5%88%8A%22%2C%22url%22%3A%22%2Fsearch%2FsearchList.do%3FsearchType%3Dperio%26showType%3Ddetail%26searchWord%3D%2B%25E5%2588%258A%25E5%2590%258D%253A%25E5%25A4%25A7%25E5%25AD%25A6%25E5%259B%25BE%25E4%25B9%25A6%25E9%25A6%2586%25E5%25AD%25A6%25E6%258A%25A5%26isTriggerTag%3D%22%7D%2C%7B%22addEndTIme%22%3A%22%22%2C%22addStartTime%22%3A%22%22%2C%22count%22%3A8048%2C%22date%22%3A%222018-01-01%22%2C%22id%22%3A1514786308634%2C%22strategy%22%3A%22+%E5%88%8A%E5%90%8D%3A%E4%B8%AD%E5%9B%BD%E5%9B%BE%E4%B9%A6%E9%A6%86%E5%AD%A6%E6%8A%A5%22%2C%22type%22%3A%22perio%22%2C%22typeName%22%3A%22%E6%9C%9F%E5%88%8A%22%2C%22url%22%3A%22%2Fsearch%2FsearchList.do%3FsearchType%3Dperio%26showType%3D%26searchWord%3D%2B%25E5%2588%258A%25E5%2590%258D%253A%25E4%25B8%25AD%25E5%259B%25BD%25E5%259B%25BE%25E4%25B9%25A6%25E9%25A6%2586%25E5%25AD%25A6%25E6%258A%25A5%26isTriggerTag%3D%22%7D%2C%7B%22addEndTIme%22%3A%22%22%2C%22addStartTime%22%3A%22%22%2C%22count%22%3A0%2C%22date%22%3A%222018-01-01%22%2C%22id%22%3A1514786305311%2C%22strategy%22%3A%22+%E5%88%8A%E5%90%8D%3A%E4%B8%AD%E5%9B%BD%E5%9B%BE%E4%B9%A6%E9%A6%86%E5%AF%BB%22%2C%22type%22%3A%22perio%22%2C%22typeName%22%3A%22%E6%9C%9F%E5%88%8A%22%2C%22url%22%3A%22%2Fsearch%2FsearchList.do%3FsearchType%3Dperio%26showType%3D%26searchWord%3D%2B%25E5%2588%258A%25E5%2590%258D%253A%25E4%25B8%25AD%25E5%259B%25BD%25E5%259B%25BE%25E4%25B9%25A6%25E9%25A6%2586%25E5%25AF%25BB%26isTriggerTag%3D%22%7D%2C%7B%22addEndTIme%22%3A%22%22%2C%22addStar; SEARCHHISTORY_1=tTime%22%3A%22%22%2C%22count%22%3A26326%2C%22date%22%3A%222018-01-01%22%2C%22id%22%3A1514785546279%2C%22strategy%22%3A%22+%E5%88%8A%E5%90%8D%3A%E5%9B%BE%E4%B9%A6%E6%83%85%E6%8A%A5%E5%B7%A5%E4%BD%9C%22%2C%22type%22%3A%22perio%22%2C%22typeName%22%3A%22%E6%9C%9F%E5%88%8A%22%2C%22url%22%3A%22%2Fsearch%2FsearchList.do%3FsearchType%3Dperio%26showType%3D%26searchWord%3D%2B%25E5%2588%258A%25E5%2590%258D%253A%25E5%259B%25BE%25E4%25B9%25A6%25E6%2583%2585%25E6%258A%25A5%25E5%25B7%25A5%25E4%25BD%259C%26isTriggerTag%3D%22%7D%2C%7B%22addEndTIme%22%3A%22%22%2C%22addStartTime%22%3A%22%22%2C%22count%22%3A4268%2C%22date%22%3A%222018-01-01%22%2C%22id%22%3A1514785117819%2C%22strategy%22%3A%22+%E5%88%8A%E5%90%8D%3A%E6%83%85%E6%8A%A5%E5%AD%A6%E6%8A%A5%22%2C%22type%22%3A%22perio%22%2C%22typeName%22%3A%22%E6%9C%9F%E5%88%8A%22%2C%22url%22%3A%22%2Fsearch%2FsearchList.do%3FsearchType%3Dperio%26pageSize%3D20%26page%3D2%26searchWord%3D%2520%25E5%2588%258A%25E5%2590%258D%3A%25E6%2583%2585%25E6%258A%25A5%25E5%25AD%25A6%25E6%258A%25A5%26order%3Dcorrelation%26showType%3Ddetail%26isCheck%3Dcheck%26isHit%3D%26isHitUnit%3D%26firstAuthor%3Dfalse%26rangeParame%3Dall%22%7D%2C%7B%22addEndTIme%22%3A%22%22%2C%22addStartTime%22%3A%22%22%2C%22count%22%3A4268%2C%22date%22%3A%222018-01-01%22%2C%22id%22%3A1514784929205%2C%22strategy%22%3A%22+%E5%88%8A%E5%90%8D%3A%E6%83%85%E6%8A%A5%E5%AD%A6%E6%8A%A5%22%2C%22type%22%3A%22perio%22%2C%22typeName%22%3A%22%E6%9C%9F%E5%88%8A%22%2C%22url%22%3A%22%2Fsearch%2FsearchList.do%3FsearchType%3Dperio%26showType%3D%26searchWord%3D%2B%25E5%2588%258A%25E5%2590%258D%253A%25E6%2583%2585%25E6%258A%25A5%25E5%25AD%25A6%25E6%258A%25A5%26isTriggerTag%3D%22%7D%5D; Hm_lpvt_838fbc4154ad87515435bf1e10023fab=1514792437");
        heaers.put("Host","new.wanfangdata.com.cn");
    }

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
        ExecutorService service = Executors.newFixedThreadPool(50);
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
            HttpResponse response = HttpUtils.httpGet(url, heaers);
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
                        HttpResponse response = HttpUtils.httpGet(paper.getPaperUrl(), heaers);
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