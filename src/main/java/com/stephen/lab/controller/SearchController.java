package com.stephen.lab.controller;

import com.stephen.lab.model.condition.PaperSearchCondition;
import com.stephen.lab.service.PaperSearchService;
import com.stephen.lab.service.SolrService;
import com.stephen.lab.util.ConditionUtil;
import com.stephen.lab.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("search")
public class SearchController {
    @Autowired
    private PaperSearchService paperSearchService;
    @Autowired
    private SolrService solrService;

    @RequestMapping("/paper")
    @ResponseBody
    public Response searchPaper(@RequestParam("keyword") String keyword, @RequestParam("searchType") Integer searchType,
                                @RequestParam("sources") List<Integer> sources,
                                @RequestParam(value = "startDate", required = false) String startDate,
                                @RequestParam(value = "endDate", required = false) String endDate,
                                @RequestParam(value = "organ", required = false) String organ,
                                @RequestParam("pageNo")Integer pageNo,
                                @RequestParam("pageSize")Integer pageSize
    ) {
        PaperSearchCondition searchCondition = ConditionUtil.getCondition(keyword, searchType, sources,
                startDate, endDate, organ,pageNo,pageSize);
        return Response.success(paperSearchService.searchPaper(searchCondition));
    }

    @RequestMapping("/paper/add")
    @ResponseBody
    public Response addPaper() {
        return Response.success(solrService.addPaperIndex());
    }
}
