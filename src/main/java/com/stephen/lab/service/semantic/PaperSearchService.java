package com.stephen.lab.service.semantic;

import com.github.pagehelper.PageInfo;
import com.stephen.lab.dto.semantic.PaperDto;
import com.stephen.lab.model.condition.PaperSearchCondition;

/**
 * Created by stephen on 2017/7/15.
 */
public interface PaperSearchService {
    PageInfo<PaperDto> searchPaper(PaperSearchCondition searchCondition);
}
