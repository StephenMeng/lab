package com.stephen.lab.controller.crawler.sjd;

import com.stephen.lab.constant.crawler.UrlConstant;
import com.stephen.lab.model.crawler.ShiJiuDaMessage;
import com.stephen.lab.service.crawler.CrawlErrorService;
import com.stephen.lab.service.crawler.ShiJiuDaService;
import com.stephen.lab.util.HttpUtils;
import com.stephen.lab.util.LogRecod;
import com.stephen.lab.util.Response;
import com.stephen.lab.util.paser.sjd.NeiMengGuParser;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by stephen on 2017/10/22.
 */
@RestController
@RequestMapping("sjd_nmg_crawler")
public class NeiMengGuController {
    @Autowired
    private CrawlErrorService crawlErrorService;
    @Autowired
    private ShiJiuDaService shiJiuDaService;

    @RequestMapping("simple")
    public Response crawlSimpleData() {
        List<String>collumn=new ArrayList<>();
        collumn.add("zxbd");
        collumn.add("sjdzxw");
        collumn.add("sh");
        collumn.add("jjsjd");
        collumn.add("fc");
        collumn.add("ry");
        collumn.add("sjddbfc");
        collumn.add("wmszh");
        collumn.add("wlzsjdzt");
        for (int i = 0; i <collumn.size(); i++) {
            String url = UrlConstant.NEI_MENG_GU +
                    "/zt/dlfjdwn/"+collumn.get(i)+"/";
            LogRecod.print(url);
            try {
                List<ShiJiuDaMessage> messages = getSimpleInfoOfPage(url);
                LogRecod.print(messages);
                messages.forEach(shiJiuDaMessage -> shiJiuDaService.addShiJiuDaItem(shiJiuDaMessage));
            } catch (Exception e) {
                crawlErrorService.addErrorItem(url, UrlConstant.NEI_MENG_GU_ID);
                e.printStackTrace();
            }
        }
        return Response.success(true);
    }

    private List<ShiJiuDaMessage> getSimpleInfoOfPage(String url) throws Exception {
        HttpResponse response = HttpUtils.httpGet(url);
        String html = IOUtils.toString(response.getEntity().getContent(), "utf-8");
        List<ShiJiuDaMessage> messages = new NeiMengGuParser().parse(html);
        return messages;
    }

    @RequestMapping("detail")
    public Response crawlDetailData() {
        ShiJiuDaMessage condition = new ShiJiuDaMessage();
        condition.setSourceId(UrlConstant.NEI_MENG_GU_ID);
        List<ShiJiuDaMessage> messageList = shiJiuDaService.select(condition);
        for (ShiJiuDaMessage shiJiuDaMessage : messageList) {
            if (shiJiuDaMessage.getTitle() == null) {
                String href = shiJiuDaMessage.getHref();
                getDetailOfItem(href);
//                break;
            }
        }
        return Response.success(true);
    }

    private void getDetailOfItem(String href) {
        try {
            HttpResponse response = HttpUtils.httpGet(href);
            String html = IOUtils.toString(response.getEntity().getContent(), "utf-8");
            ShiJiuDaMessage result = new NeiMengGuParser().parseDetail(html);
            LogRecod.print(result);
            result.setHref(href);
            if (result.getTitle() != null) {
                shiJiuDaService.updateSelective(result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
