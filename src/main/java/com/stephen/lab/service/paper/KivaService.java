package com.stephen.lab.service.paper;

import com.stephen.lab.model.paper.Kiva;

import java.util.List;

public interface KivaService {
    int insert(Kiva kiva);

    List<Kiva> select(Kiva kiva);

    Kiva selectOne(Kiva kiva);

    List<Kiva> selectAll();
}
