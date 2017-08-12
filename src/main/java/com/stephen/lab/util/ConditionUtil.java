package com.stephen.lab.util;

import com.stephen.lab.model.condition.PaperSearchCondition;

public class ConditionUtil {
    public static PaperSearchCondition getCondition(String keyword, Integer searchType, String startDate, String endDate, String organ) {
        PaperSearchCondition condition = new PaperSearchCondition();
        condition.setKeyword(keyword);
//        switch (searchType){
//            case PaperConditionType.TITLE:
//                condition.sett
//                condition
//                break;
//            case PaperConditionType.TITLE:
//                break;
//            case PaperConditionType.TITLE:
//                break;
//        }
        return condition;
    }
}
