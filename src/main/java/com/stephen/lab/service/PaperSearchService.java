package com.stephen.lab.service;

import com.github.pagehelper.PageInfo;
import com.stephen.lab.model.Paper;
import com.stephen.lab.model.condition.PaperSearchCondition;

/**
 * Created by stephen on 2017/7/15.
 */
public interface PaperSearchService {
    PageInfo<Paper> searchPaper(PaperSearchCondition searchCondition);
}
