package com.stephen.lab.controller.crawler.sjd;

import com.stephen.lab.constant.crawler.UrlConstant;
import com.stephen.lab.model.crawler.ShiJiuDaMessage;
import com.stephen.lab.service.crawler.CrawlErrorService;
import com.stephen.lab.service.crawler.ShiJiuDaService;
import com.stephen.lab.util.HttpUtils;
import com.stephen.lab.util.Response;
import com.stephen.lab.util.paser.sjd.ChinaDailyParser;
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
@RequestMapping("cd_crawler")
public class ChinaDailyController {
    @Autowired
    private CrawlErrorService crawlErrorService;
    @Autowired
    private ShiJiuDaService shiJiuDaService;

    @RequestMapping("simple")
    public Response crawlSimpleData() {

        for (int i = 1; i <= 10; i++) {
            String url = UrlConstant.CHINA_DAILY_COM +
                    "/19thcpcnationalcongress/node_53011556_" + i +
                    ".htm";
            try {
                List<ShiJiuDaMessage> messages = getSimpleInfoOfPage(url);

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
        List<ShiJiuDaMessage> messages = new ChinaDailyParser().parse(html);
        return messages;
    }

    @RequestMapping("detail")
    public Response crawlDetailData() {
        ShiJiuDaMessage condition = new ShiJiuDaMessage();
        condition.setSourceId(UrlConstant.CHINA_DAILY_COM_ID);
        List<ShiJiuDaMessage> messageList = shiJiuDaService.select(condition);
        for (ShiJiuDaMessage shiJiuDaMessage : messageList) {
            if (shiJiuDaMessage.getTitle() == null) {
                String href = shiJiuDaMessage.getHref();
                getDetailOfItem(href);
            }
//            break;
        }
        return Response.success(true);
    }

    private void getDetailOfItem(String href) {
        try {
            HttpResponse response = HttpUtils.httpGet(href);
            String html = IOUtils.toString(response.getEntity().getContent(), "utf-8");
            ShiJiuDaMessage result = new ChinaDailyParser().parseDetail(html);
            result.setHref(href);
            if (result.getTitle() != null) {
                shiJiuDaService.updateSelective(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
