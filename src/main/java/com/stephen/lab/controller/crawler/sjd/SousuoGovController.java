package com.stephen.lab.controller.crawler.sjd;

import com.stephen.lab.constant.crawler.UrlConstant;
import com.stephen.lab.model.crawler.ShiJiuDaMessage;
import com.stephen.lab.service.crawler.CrawlErrorService;
import com.stephen.lab.service.crawler.ShiJiuDaService;
import com.stephen.lab.util.HttpUtils;
import com.stephen.lab.util.LogRecod;
import com.stephen.lab.util.Response;
import com.stephen.lab.util.paser.Parser;
import com.stephen.lab.util.paser.sjd.SousuoGovPaser;
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
@RequestMapping("gov_crawler")
public class SousuoGovController {
    @Autowired
    private CrawlErrorService crawlErrorService;
    @Autowired
    private ShiJiuDaService shiJiuDaService;

    @RequestMapping("simple")
    public Response crawlSimpleData() {
        for (int i = 0; i <= 11; i++) {
            String url = UrlConstant.SOUSUO_GOV_CN
                    + "/s.htm?q=&n=100&p=" + i +
                    "&t=govall&advance=true&orpro=%E5%8D%81%E4%B9%9D%E5%A4%A7&andpro=&notpro=&inpro=&pubmintimeYear=2017&pubmintimeMonth=10&pubmintimeDay=&pubmaxtimeYear=2017&pubmaxtimeMonth=12&pubmaxtimeDay=&searchfield=title&colid=&timetype=timeqb&mintime=&maxtime=&sort=&sortType=1&nocorrect=";
            LogRecod.print(url);
            getSimpleInfoOfPage(url);
        }
        return Response.success(true);
    }

    private void getSimpleInfoOfPage(String url) {
        try {
            HttpResponse response = HttpUtils.httpGet(url);
            String html = IOUtils.toString(response.getEntity().getContent(), "gbk");
            Parser<ShiJiuDaMessage> parser = new SousuoGovPaser();
            List<ShiJiuDaMessage> messages = parser.parse(html);
            messages.forEach(shiJiuDaMessage -> shiJiuDaService.addShiJiuDaItem(shiJiuDaMessage));
        } catch (Exception e) {
            crawlErrorService.addErrorItem(url, UrlConstant.SOUSUO_GOV_CN_ID);
            e.printStackTrace();
        }
    }

    @RequestMapping("detail")
    public Response crawlDetailData() {
        ShiJiuDaMessage condition = new ShiJiuDaMessage();
        condition.setSourceId(UrlConstant.SOUSUO_GOV_CN_ID);
        List<ShiJiuDaMessage> messageList = shiJiuDaService.select(condition);
        for (ShiJiuDaMessage shiJiuDaMessage : messageList) {
            if (shiJiuDaMessage.getTitle() == null) {
                getDetailOfItem(shiJiuDaMessage.getHref());
            }
//            break;
        }
        return Response.success(true);
    }

    private void getDetailOfItem(String href) {
        try {
            HttpResponse response = HttpUtils.httpGet(href);
            String html = IOUtils.toString(response.getEntity().getContent(), "utf-8");
            Parser<ShiJiuDaMessage> parser = new SousuoGovPaser();
            ShiJiuDaMessage result = parser.parseDetail(html);
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
