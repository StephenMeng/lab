package com.stephen.lab.controller.crawler.sjd;

import com.stephen.lab.constant.crawler.UrlConstant;
import com.stephen.lab.model.crawler.ShiJiuDaMessage;
import com.stephen.lab.service.crawler.CrawlErrorService;
import com.stephen.lab.service.crawler.ShiJiuDaService;
import com.stephen.lab.util.HttpUtils;
import com.stephen.lab.util.LogRecod;
import com.stephen.lab.util.Response;
import com.stephen.lab.util.paser.Parser;
import com.stephen.lab.util.paser.sjd.SinaSearchPaser;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * Created by stephen on 2017/10/22.
 */
@RestController
@RequestMapping("sina_crawler")
public class SinaSearchController {
    @Autowired
    private CrawlErrorService crawlErrorService;
    @Autowired
    private ShiJiuDaService shiJiuDaService;

    @RequestMapping("simple")
    public Response crawlSimpleData() {
        for (int i = 1; i <= 112; i++) {
            String url = UrlConstant.SEARCH_SINA_COM + "/?"
                    + "c=news&q=十九大&range=title&time=w&stime=&etime=&num=20&col=&source=&from=" +
                    "&country=&size=&a=&page=" + i +
                    "&pf=2131425450&ps=2134309112&dpc=1";
            getSimpleInfoOfPage(url);
        }
        return Response.success(true);
    }

    private void getSimpleInfoOfPage(String url) {
        try {
            HttpResponse response = HttpUtils.httpGet(url);
            String html = IOUtils.toString(response.getEntity().getContent(), "gbk");
            Parser<ShiJiuDaMessage> parser = new SinaSearchPaser();
            List<ShiJiuDaMessage> messages = parser.parse(html);
            messages.forEach(shiJiuDaMessage -> shiJiuDaService.addShiJiuDaItem(shiJiuDaMessage));
        } catch (Exception e) {
            crawlErrorService.addErrorItem(url, UrlConstant.SEARCH_SINA_COM_ID);
            e.printStackTrace();
        }
    }

    @RequestMapping("detail")
    public Response crawlDetailData() {
        ShiJiuDaMessage condition = new ShiJiuDaMessage();
        condition.setSourceId(UrlConstant.SEARCH_SINA_COM_ID);
        List<ShiJiuDaMessage> messageList = shiJiuDaService.select(condition);

        for (ShiJiuDaMessage shiJiuDaMessage : messageList) {
            if (shiJiuDaMessage.getTitle() == null && shiJiuDaMessage.getHref().contains("/news.")) {
                String href=shiJiuDaMessage.getHref();
                getDetailOfItem(href);
            }
        }
        return Response.success(true);
    }

    private void getDetailOfItem(String href) {
        try {
            HttpResponse response = HttpUtils.httpGet(href);
            String html = IOUtils.toString(response.getEntity().getContent(), "utf-8");
            Parser<ShiJiuDaMessage> sinaSearchPaser = new SinaSearchPaser();
            ShiJiuDaMessage result = sinaSearchPaser.parseDetail(html);
            result.setHref(href);
            LogRecod.print(result);
            if (result.getTitle() != null) {
                shiJiuDaService.updateSelective(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
