package com.stephen.lab.service.semantic;

import com.stephen.lab.model.semantic.Paper;

import java.util.List;

/**
 * Created by stephen on 2017/7/15.
 */
public interface PaperService {
    Boolean index();

    Paper selectByPaperId(Long paperId);

    void addPaper(Paper paper);

    List<Paper> select(Paper conditon);

    int updateSelective(Paper p);
}
