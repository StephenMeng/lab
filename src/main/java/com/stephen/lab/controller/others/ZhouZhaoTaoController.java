package com.stephen.lab.controller.others;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.Files;
import com.stephen.lab.util.MapUtils;
import com.stephen.lab.util.Response;
import com.stephen.lab.util.StringUtils;
import io.swagger.models.auth.In;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by stephen on 2018/3/15.
 */
@RestController
@RequestMapping("zzt")
public class ZhouZhaoTaoController {
    @RequestMapping("high-freq")
    public Response highFreq() throws IOException {
        String fileInputPath = "C:\\Users\\stephen\\Desktop\\high-freq.txt";
        String fileOutputPath = "C:\\Users\\stephen\\Desktop\\high-freq-result.txt";

        List<String> stringList = Files.readLines(new File(fileInputPath), Charsets.UTF_8);
        Map<String, Integer> wordFreqMap = new HashMap<>();
        stringList.forEach(s -> {
            List<String> strings = Splitter.on(";").trimResults().splitToList(s);
            strings.forEach(string -> {
                if(!StringUtils.isNull(string)) {
                    if (wordFreqMap.containsKey(string.toLowerCase())) {
                        wordFreqMap.put(string.toLowerCase(), wordFreqMap.get(string.toLowerCase()) + 1);
                    } else {
                        wordFreqMap.put(string.toLowerCase(), 1);
                    }
                }
            });
        });
        File outF = new File(fileOutputPath);
        Map<String, Integer> result = MapUtils.sortMapByValue(wordFreqMap, true);
        for (Map.Entry<String, Integer> map : result.entrySet()) {
            Files.append(map.getKey() + "\t" + map.getValue()+"\r\n", outF, Charsets.UTF_8);
        }
        return Response.success("");
    }
}
