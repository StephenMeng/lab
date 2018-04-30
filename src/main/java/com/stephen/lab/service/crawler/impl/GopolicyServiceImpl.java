package com.stephen.lab.service.crawler.impl;

import com.stephen.lab.dao.crawler.GopolicyDao;
import com.stephen.lab.model.crawler.Gopolicy;
import com.stephen.lab.service.crawler.GopolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GopolicyServiceImpl implements GopolicyService {
    @Autowired
    private GopolicyDao gopolicyDao;

    @Override
    public int insert(Gopolicy gopolicy) {
        try {
            return gopolicyDao.insert(gopolicy);
        } catch (Exception e) {
        }
        return 0;
    }

    @Override
    public int updateSelective(Gopolicy gopolicy) {
        return gopolicyDao.updateByPrimaryKeySelective(gopolicy);
    }

    @Override
    public List<Gopolicy> selectAll() {
        return gopolicyDao.selectAll();
    }

    @Override
    public List<Gopolicy> selectNullTitle() {
        return gopolicyDao.selectNullTitle();
    }

    @Override
    public List<Gopolicy> selectNullFullText() {
        return gopolicyDao.selectNullFullText();
    }
}
