package com.stephen.lab.util;

import com.stephen.lab.model.condition.PaperSearchCondition;

import java.util.List;

public class ConditionUtil {
    public static PaperSearchCondition getCondition(String keyword, Integer searchType, Integer sources, String startDate, String endDate, String organ, Integer pageNo, Integer pageSize) {
        PaperSearchCondition condition = new PaperSearchCondition();
        condition.setQ(keyword);
        condition.setSearchType(searchType);
        condition.setStartDate(TimeFormateUtil.parseStringToDate(startDate, "yyyy-MM-dd"));
        condition.setEndDate(TimeFormateUtil.parseStringToDate(endDate, "yyyy-MM-dd"));
        condition.setSources(sources);
        condition.setPageNo(pageNo);
        condition.setPageSize(pageSize);
        return condition;
    }
}
