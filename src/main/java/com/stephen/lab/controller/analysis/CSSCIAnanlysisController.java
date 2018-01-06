package com.stephen.lab.controller.analysis;

import com.stephen.lab.constant.crawler.UrlConstant;
import com.stephen.lab.model.semantic.Paper;
import com.stephen.lab.service.semantic.PaperService;
import com.stephen.lab.util.Response;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("analysis/cssci")
public class CSSCIAnanlysisController
{
    @Autowired
    private PaperService paperService;
    @RequestMapping("parseKeyword")
    public Response parseKeyword(){
        Paper condition=new Paper();
        condition.setSource(UrlConstant.CSSCI_ID);
        List<Paper> paperList=paperService.select(condition);
//        Map<Integer,List<Paper>>yearPapers=new HashMap<>();
        List<String>keywords=new ArrayList<>();
        paperList.forEach(paper -> {
            if(StringUtils.isBlank(paper.getKeyword())){

            }
        });
        return Response.success("");
    }
}
