package com.stephen.lab.service.paper;

import com.stephen.lab.model.paper.Kiva;
import com.stephen.lab.model.paper.KivaSimple;

import java.util.List;

public interface KivaService {
    int insert(Kiva kiva);

    List<Kiva> select(Kiva kiva);

    Kiva selectOne(Kiva kiva);

    List<Kiva> selectAll(int num);

    int insertKivaSimple(KivaSimple simple);

    List<KivaSimple> selectAllSimple(int docNum);

    List<KivaSimple> select(KivaSimple condition);

    KivaSimple selectOne(KivaSimple condition);

    int updateSimpleSelective(KivaSimple simple);

    KivaSimple selectSimpleById(long id);

    List<Long> selectSimpleIdLikeTag(String tag);
}
