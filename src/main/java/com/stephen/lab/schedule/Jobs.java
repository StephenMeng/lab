package com.stephen.lab.schedule;

import com.stephen.lab.util.solr.SolrAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;


public class Jobs {
    @Autowired
    private SolrAdapter solrAdapter;
    @Scheduled(cron = "0 1 0 0 0 0")
    public void test(){

    }

}
