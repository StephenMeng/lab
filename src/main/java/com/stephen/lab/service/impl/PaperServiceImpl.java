package com.stephen.lab.service.impl;

import com.stephen.lab.dao.PaperDao;
import com.stephen.lab.model.Paper;
import com.stephen.lab.service.PaperService;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PaperServiceImpl implements PaperService {
    @Autowired
    private HttpSolrClient solrClient;
    @Autowired
    private PaperDao paperDao;

    @Override
    public Boolean index() {
        try {
            SolrQuery query = new SolrQuery();
            query.setRequestHandler("/dataimport");
            query.setParam("full_import", true);
            query.setParam("Verbose", false);
            query.setParam("Clean", true);
            query.setParam("Commit", true);
            solrClient.query(query);
            solrClient.commit();
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public Paper selectByPaperId(Long paperId) {
        return paperDao.selectByPrimaryKey(paperId);
    }
}
