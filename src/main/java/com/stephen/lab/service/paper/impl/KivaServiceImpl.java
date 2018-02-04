package com.stephen.lab.service.paper.impl;

import com.stephen.lab.dao.paper.KivaDao;
import com.stephen.lab.model.paper.Kiva;
import com.stephen.lab.service.paper.KivaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KivaServiceImpl implements KivaService {
    @Autowired
    private KivaDao kivaDao;

    @Override
    public int insert(Kiva kiva) {
        return kivaDao.insert(kiva);
    }

    @Override
    public List<Kiva> select(Kiva kiva) {
        return kivaDao.select(kiva);
    }

    @Override
    public Kiva selectOne(Kiva kiva) {
        return kivaDao.selectOne(kiva);
    }
}
