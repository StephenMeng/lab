package com.stephen.lab.controller.crawler.sjd;

import com.stephen.lab.constant.crawler.UrlConstant;
import com.stephen.lab.model.crawler.ShiJiuDaMessage;
import com.stephen.lab.service.crawler.CrawlErrorService;
import com.stephen.lab.service.crawler.ShiJiuDaService;
import com.stephen.lab.util.HttpUtils;
import com.stephen.lab.util.LogRecod;
import com.stephen.lab.util.Response;
import com.stephen.lab.util.paser.sjd.GuangDongParser;
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
@RequestMapping("sjd_gd_crawler")
public class GuangDongController {
    @Autowired
    private CrawlErrorService crawlErrorService;
    @Autowired
    private ShiJiuDaService shiJiuDaService;

    @RequestMapping("simple")
    public Response crawlSimpleData() {

        for (int i = 2; i <= 50; i++) {
            String url = UrlConstant.SOUTH_CN +
                    "/pc2016/yw/node_346416" +
                    ".htm";
            LogRecod.print(url);
            try {
                List<ShiJiuDaMessage> messages = getSimpleInfoOfPage(url);
                LogRecod.print(messages);
                messages.forEach(shiJiuDaMessage -> shiJiuDaService.addShiJiuDaItem(shiJiuDaMessage));
            } catch (Exception e) {
                crawlErrorService.addErrorItem(url, UrlConstant.CHINA_DAILY_COM_ID);
                e.printStackTrace();
            }
        }
        return Response.success(true);
    }

    private List<ShiJiuDaMessage> getSimpleInfoOfPage(String url) throws Exception {
        HttpResponse response = HttpUtils.httpGet(url);
        String html = IOUtils.toString(response.getEntity().getContent(), "utf-8");
        List<ShiJiuDaMessage> messages = new GuangDongParser().parse(html);
        return messages;
    }

    @RequestMapping("detail")
    public Response crawlDetailData() {
        ShiJiuDaMessage condition = new ShiJiuDaMessage();
        condition.setSourceId(UrlConstant.SOUTH_CN_ID);
        List<ShiJiuDaMessage> messageList = shiJiuDaService.select(condition);
        for (ShiJiuDaMessage shiJiuDaMessage : messageList) {
            if (shiJiuDaMessage.getTitle() == null) {
                String href = shiJiuDaMessage.getHref();
                getDetailOfItem(href);
            }
        }
        return Response.success(true);
    }

    private void getDetailOfItem(String href) {
        try {
            HttpResponse response = HttpUtils.httpGet(href);
            String html = IOUtils.toString(response.getEntity().getContent(), "utf-8");
            ShiJiuDaMessage result = new GuangDongParser().parseDetail(html);
            result.setHref(href);
            if (result.getTitle() != null) {
                shiJiuDaService.updateSelective(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
