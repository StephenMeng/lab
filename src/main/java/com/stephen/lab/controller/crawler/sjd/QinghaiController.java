package com.stephen.lab.controller.crawler.sjd;

import com.stephen.lab.constant.crawler.UrlConstant;
import com.stephen.lab.model.crawler.ShiJiuDaMessage;
import com.stephen.lab.service.crawler.CrawlErrorService;
import com.stephen.lab.service.crawler.ShiJiuDaService;
import com.stephen.lab.util.HttpUtils;
import com.stephen.lab.util.LogRecod;
import com.stephen.lab.util.Response;
import com.stephen.lab.util.paser.sjd.QinghaiParser;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by stephen on 2017/10/22.
 */
@RestController
@RequestMapping("sjd_qh_crawler")
public class QinghaiController {
    @Autowired
    private CrawlErrorService crawlErrorService;
    @Autowired
    private ShiJiuDaService shiJiuDaService;

    @RequestMapping("simple")
    public Response crawlSimpleData() {

        for (int i = 1; i <= 75; i++) {
            String url = UrlConstant.SOU_QHNEWS_COM +
                    "/m_fullsearch/full_search1.jsp";
            Map<String, String> map = new HashMap<>();
            map.put("size", "200");
            map.put("header", "");
            map.put("footer", "");
            map.put("sort", "2");
            map.put("keywords", "%CA%AE%BE%C5%B4%F3");
            map.put("channel_id", "0");
            map.put("pagen", i+"");

            try {
                List<ShiJiuDaMessage> messages = getSimpleInfoOfPage(url, map);
                messages.forEach(shiJiuDaMessage -> shiJiuDaService.addShiJiuDaItem(shiJiuDaMessage));
            } catch (Exception e) {
                crawlErrorService.addErrorItem(url, UrlConstant.CHINA_DAILY_COM_ID);
                e.printStackTrace();
            }
//            break;
        }
        return Response.success(true);
    }

    private List<ShiJiuDaMessage> getSimpleInfoOfPage(String url, Map<String, String> map) throws Exception {
        HttpResponse response = HttpUtils.httpPost(url, map);
        String html = IOUtils.toString(response.getEntity().getContent(), "gbk");
        List<ShiJiuDaMessage> messages = new QinghaiParser().parse(html);
        return messages;
    }

    @RequestMapping("detail")
    public Response crawlDetailData() {
        ShiJiuDaMessage condition = new ShiJiuDaMessage();
        condition.setSourceId(UrlConstant.SOU_QHNEWS_COM_ID);
        List<ShiJiuDaMessage> messageList = shiJiuDaService.select(condition);
        for (ShiJiuDaMessage shiJiuDaMessage : messageList) {
            if (shiJiuDaMessage.getTitle() == null&&shiJiuDaMessage.getHref().contains("www.qhnews.com")) {
                String href = shiJiuDaMessage.getHref();
                getDetailOfItem(href);
            }
        }
        return Response.success(true);
    }

    private void getDetailOfItem(String href) {
        try {
            HttpResponse response = HttpUtils.httpGet(href);
            String html = IOUtils.toString(response.getEntity().getContent(), "gbk");
            ShiJiuDaMessage result = new QinghaiParser().parseDetail(html);
            LogRecod.print(result);
            result.setHref(href);
            if (result.getTitle() != null) {
//                shiJiuDaService.updateSelective(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
