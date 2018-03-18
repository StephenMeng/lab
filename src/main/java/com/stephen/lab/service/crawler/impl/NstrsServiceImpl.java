package com.stephen.lab.service.crawler.impl;

import com.stephen.lab.dao.crawler.NstrsDao;
import com.stephen.lab.model.crawler.Nstrs;
import com.stephen.lab.service.crawler.NstrsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NstrsServiceImpl implements NstrsService {
    @Autowired
    private NstrsDao nstrsDao;

    @Override
    public int add(Nstrs nstrs) {
        return nstrsDao.insert(nstrs);
    }

    @Override
    public List<Nstrs> selectAll() {
        return nstrsDao.selectAll();
    }

    @Override
    public void updateSelective(Nstrs nstrs) {
        nstrsDao.updateByPrimaryKeySelective(nstrs);
    }

    @Override
    public Nstrs selectOne(Nstrs nstrs) {
        Nstrs r = null;
        try {
            r = nstrsDao.selectOne(nstrs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;

    }
}
