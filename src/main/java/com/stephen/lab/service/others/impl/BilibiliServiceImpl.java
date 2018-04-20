package com.stephen.lab.service.others.impl;

import com.stephen.lab.dao.others.BilibiliDao;
import com.stephen.lab.model.others.Bilibili;
import com.stephen.lab.service.others.BilibiliService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BilibiliServiceImpl implements BilibiliService {
    @Autowired
    private BilibiliDao bilibiliDao;

    @Override
    public int insert(Bilibili bilibili) {
        return bilibiliDao.insert(bilibili);
    }
}
