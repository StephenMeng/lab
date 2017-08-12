package com.stephen.lab.service;

import com.stephen.lab.model.Paper;

/**
 * Created by stephen on 2017/7/15.
 */
public interface PaperService {
    Boolean index();

    Paper selectByPaperId(Long paperId);
}
