package com.stephen.lab.service.impl;

import com.stephen.lab.constant.SolrConstant;
import com.stephen.lab.service.SolrService;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SolrServiceImpl implements SolrService {
    @Autowired
    private HttpSolrClient solrClient;
    @Override
    public Boolean addPaperIndex() {
        Long pid = (long) 5;
        String keyword = "我是个德国人 keyword";
        String title = "我是个德国人 title";
        String summary = "我是个德国人 summary";


        try {
            SolrInputDocument inputDocument = new SolrInputDocument();
            inputDocument.setField("id", pid);
            inputDocument.setField(SolrConstant.PAPER_KEYWORD, keyword);
            inputDocument.setField(SolrConstant.PAPER_TITLE, title);
            inputDocument.setField(SolrConstant.PAPER_SUMMARY, summary);

            solrClient.add(inputDocument);
            solrClient.commit();
            return true;
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Boolean updatePaperIndex() {
        Long pid = (long) 5;
        String keyword = "我是个德国人 keyword2";
        String title = "我是个德国人 title2";
        String summary = "我是个德国人 summary2";
        try {
            SolrInputDocument inputDocument = new SolrInputDocument();
            inputDocument.addField("id", pid);
            inputDocument.addField(SolrConstant.PAPER_KEYWORD, keyword);
            inputDocument.addField(SolrConstant.PAPER_TITLE, title);
            inputDocument.addField(SolrConstant.PAPER_SUMMARY, summary);
            solrClient.add(inputDocument);
            solrClient.commit();
            return true;
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Boolean deletePaperIndex(Long paperId) {
        try {
            solrClient.deleteById(String.valueOf(paperId));
            return true;
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
