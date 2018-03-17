package com.stephen.lab.service.paper.impl;

import com.stephen.lab.dao.paper.KivaDao;
import com.stephen.lab.dao.paper.KivaSimpleDao;
import com.stephen.lab.model.paper.Kiva;
import com.stephen.lab.model.paper.KivaSimple;
import com.stephen.lab.service.paper.KivaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KivaServiceImpl implements KivaService {
    @Autowired
    private KivaDao kivaDao;
    @Autowired
    private KivaSimpleDao kivaSimpleDao;

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

    @Override
    public List<Kiva> selectAll() {
        return kivaDao.selectAll();
    }

    @Override
    public int insertKivaSimple(KivaSimple simple) {
        return kivaSimpleDao.insert(simple);
    }

    @Override
    public List<KivaSimple> selectAllSimple() {
        return kivaSimpleDao.selectAll();
    }

    @Override
    public List<KivaSimple> select(KivaSimple condition) {
        return kivaSimpleDao.select(condition);
    }

    @Override
    public KivaSimple selectOne(KivaSimple condition) {
        return kivaSimpleDao.selectOne(condition);
    }

    @Override
    public int updateSimpleSelective(KivaSimple simple) {
        return kivaSimpleDao.updateByPrimaryKeySelective(simple);
    }

    @Override
    public KivaSimple selectSimpleById(long id) {
        return kivaSimpleDao.selectByPrimaryKey(id);
    }
}
