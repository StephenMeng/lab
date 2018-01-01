package com.stephen.lab.controller.crawler.wanfang;

import com.stephen.lab.constant.crawler.UrlConstant;
import com.stephen.lab.model.crawler.ShiJiuDaMessage;
import com.stephen.lab.model.semantic.Paper;
import com.stephen.lab.service.crawler.NstrsService;
import com.stephen.lab.service.semantic.PaperService;
import com.stephen.lab.util.HttpUtils;
import com.stephen.lab.util.LogRecod;
import com.stephen.lab.util.Response;
import com.stephen.lab.util.paser.Parser;
import com.stephen.lab.util.paser.sjd.CnrPaser;
import com.stephen.lab.util.paser.wanfang.WanFangPaser;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

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

    @RequestMapping("crawl")
    public Response crawl(@RequestParam("journal") String journal,
                          @RequestParam("pageNum") Integer pageNum,
                          @RequestParam("pageSize") Integer pageSize) throws IOException {

        for (int i = 1; i <= pageNum; i++) {
            String url = "http://new.wanfangdata.com.cn/search/searchList.do?searchType=perio&pageSize=" + pageSize
                    + "&searchWord=%20%E5%88%8A%E5%90%8D:" + journal
                    + "&showType=detail&order=correlation&isHit=&isHitUnit=" +
                    "&firstAuthor=false&rangeParame=all&page=" + i;

            HttpResponse response = HttpUtils.httpGet(url);
            if (response == null) {
            }
            String result = null;
            try {
                result = IOUtils.toString(response.getEntity().getContent());
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<Paper> messages = new WanFangPaser().parse(result);
            Integer page = i;
            messages.forEach(paper -> {
//                Paper conditon = new Paper();
//                conditon.setPaperUrl(paper.getPaperUrl());
//                List<Paper> papers = paperService.select(conditon);
//                if (papers == null || papers.size() == 0) {
                    LogRecod.print(String.valueOf(page) + "\t" + paper.getTitle());
                    paperService.addPaper(paper);
//                }
            });
            if(messages.size()<pageSize){

            }
        }
        return Response.success(true);
    }

    @RequestMapping("crawl_detail")
    public Response crawlDetial() throws IOException {
        Paper condition = new Paper();
        condition.setSource(UrlConstant.WAN_FAGN_ID);
        List<Paper> paperList = paperService.select(condition);
        Parser parser = new WanFangPaser();
        for(Paper paper:paperList){
            if (paper.getAuthor() == null ) {
                try {
                    HttpResponse response = HttpUtils.httpGet(paper.getPaperUrl());
                    String html = IOUtils.toString(response.getEntity().getContent());
                    Paper p = (Paper) parser.parseDetail(html);
                    p.setPaperId(paper.getPaperId());
                    LogRecod.print(p);
                    paperService.updateSelective(p);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            break;
        }
        return Response.success(true);
    }
}