package com.stephen.lab.service.semantic;

import com.stephen.lab.model.semantic.Paper;

/**
 * Created by stephen on 2017/7/15.
 */
public interface PaperService {
    Boolean index();

    Paper selectByPaperId(Long paperId);

    void addPaper(Paper paper);
}
