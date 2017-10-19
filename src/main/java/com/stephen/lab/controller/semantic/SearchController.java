package com.stephen.lab.controller.semantic;

import com.stephen.lab.model.condition.PaperSearchCondition;
import com.stephen.lab.service.semantic.PaperSearchService;
import com.stephen.lab.service.semantic.SolrService;
import com.stephen.lab.util.ConditionUtil;
import com.stephen.lab.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("semantic/search")
public class SearchController {
    @Autowired
    private PaperSearchService paperSearchService;
    @Autowired
    private SolrService solrService;

    /**
     * 查询论文
     *
     * @param keyword    检索词
     * @param searchType 搜索类型 1，论文名；2、论文关键词；3、论文摘要
     * @param sources    数据源
     * @param startDate  开始日期
     * @param endDate    结束日期
     * @param organ      机构
     * @param pageNo     页码
     * @param pageSize   页容量
     * @return
     */
    @RequestMapping("/paper")
    @ResponseBody
    public Response searchPaper(@RequestParam("keyword") String keyword, @RequestParam("searchType") Integer searchType,
                                @RequestParam(value = "sources", required = false) Integer sources,
                                @RequestParam(value = "startDate", required = false) String startDate,
                                @RequestParam(value = "endDate", required = false) String endDate,
                                @RequestParam(value = "organ", required = false) String organ,
                                @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize
    ) {
        PaperSearchCondition searchCondition = ConditionUtil.getCondition(keyword, searchType, sources,
                startDate, endDate, organ, pageNo, pageSize);
        return Response.success(paperSearchService.searchPaper(searchCondition));
    }

    @RequestMapping("/paper/add")
    @ResponseBody
    public Response addPaper() {
        return Response.success(solrService.addPaperIndex());
    }
}
