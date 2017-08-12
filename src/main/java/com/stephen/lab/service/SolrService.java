package com.stephen.lab.service;

/**
 * Created by stephen on 2017/7/15.
 */
public interface SolrService {
    Boolean addPaperIndex();
    Boolean updatePaperIndex();
    Boolean deletePaperIndex(Long paper);
}
