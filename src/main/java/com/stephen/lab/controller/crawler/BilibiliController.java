package com.stephen.lab.controller.crawler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.stephen.lab.constant.semantic.ResultEnum;
import com.stephen.lab.model.others.BDanmu;
import com.stephen.lab.model.others.Bilibili;
import com.stephen.lab.service.crawler.BDanmuService;
import com.stephen.lab.service.crawler.BilibiliService;
import com.stephen.lab.util.HttpUtils;
import com.stephen.lab.util.LogRecod;
import com.stephen.lab.util.Response;
import org.apache.commons.io.IOUtils;
import org.apache.xerces.impl.xpath.regex.Match;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sun.rmi.runtime.Log;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("crawl_b")
public class BilibiliController {
    @Autowired
    private BilibiliService bilibiliService;
    @Autowired
    private BDanmuService bDanmuService;

    @RequestMapping("bilibili")
    public Response parseVedioId() {
        String url = "https://search.bilibili.com/api/search?search_type=all&keyword=%E5%95%A6%E5%95%A6%E5%95%A6";
        try {
            String jsonStr = HttpUtils.okrHttpGet(url);
            JSONArray jsonArray = JSON.parseObject(jsonStr).getJSONObject("result").getJSONArray("video");
            List<Bilibili> bilibiliList = jsonArray.toJavaList(Bilibili.class);
            bilibiliList.forEach(bilibili -> {
                bilibiliService.insert(bilibili);
            });
            return Response.success(bilibiliList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Response.error(ResultEnum.FAIL_PARAM_WRONG);
    }

    @RequestMapping("danmu")
    public Response danmu() {
        List<Bilibili> bilibiliList = bilibiliService.selectAll();
        for (Bilibili bilibili : bilibiliList) {
            crawlOneVedioDanmu(bilibili.getCid());
        }
        return Response.error(ResultEnum.FAIL_PARAM_WRONG);
    }

    private Response crawlOneVedioDanmu(Long cid) {
        String url = "http://comment.bilibili.com/" + cid + ".xml";
        try {
            String xml = IOUtils.toString(HttpUtils.httpGet(url).getEntity().getContent());
            Pattern danmuPattern = Pattern.compile("<d(.*?)d>");
            Matcher danmuMatcher = danmuPattern.matcher(xml);
            while  (danmuMatcher.find()) {
                String content = danmuMatcher.group();
                BDanmu danmu = new BDanmu();
                danmu.setAid(cid);
                danmu.setContent(content);
                try {
                    bDanmuService.insert(danmu);
                } catch (Exception e) {
                    LogRecod.print(cid);
                    LogRecod.print(content);
                }
                LogRecod.print(content);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
