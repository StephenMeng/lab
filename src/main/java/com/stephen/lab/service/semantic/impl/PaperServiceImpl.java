package com.stephen.lab.service.semantic.impl;

import com.stephen.lab.dao.semantic.PaperDao;
import com.stephen.lab.model.semantic.Paper;
import com.stephen.lab.service.semantic.PaperService;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

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

    @Override
    public void addPaper(Paper paper) {
        paperDao.insert(paper);
    }

    @Override
    public List<Paper> select(Paper conditon) {
        return paperDao.select(conditon);
    }

    @Override
    public int updateSelective(Paper p) {
        return paperDao.updateByPrimaryKeySelective(p);
    }
}
