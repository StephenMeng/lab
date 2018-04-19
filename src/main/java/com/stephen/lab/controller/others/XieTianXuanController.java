package com.stephen.lab.controller.others;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.stephen.lab.constant.semantic.ResultEnum;
import com.stephen.lab.model.others.Bilibili;
import com.stephen.lab.service.others.BilibiliService;
import com.stephen.lab.util.HttpUtils;
import com.stephen.lab.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("xtx")
public class XieTianXuanController {
    @Autowired
    private BilibiliService bilibiliService;

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

}
