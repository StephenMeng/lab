package com.stephen.lab.service.impl;

import com.github.pagehelper.PageInfo;
import com.stephen.lab.constant.PaperConditionType;
import com.stephen.lab.dao.PaperDao;
import com.stephen.lab.model.Paper;
import com.stephen.lab.model.condition.PaperSearchCondition;
import com.stephen.lab.service.PaperSearchService;
import com.stephen.lab.service.PaperService;
import com.stephen.lab.util.LogRecod;
import com.stephen.lab.util.PageUtil;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PaperSearchServiceImpl implements PaperSearchService {
    @Autowired
    private PaperService paperService;
    @Autowired
    private HttpSolrClient solrClient;

    @Override
    public PageInfo<Paper> searchPaper(PaperSearchCondition searchCondition) {
        List<Paper> papers = new ArrayList<>();
        Long total = 0L;
        String q = parseSearchCondition(searchCondition);
        try {
            SolrQuery solrQuery = new SolrQuery();
            solrQuery.setQuery(q);
            solrQuery.setStart((searchCondition.getPageNo() - 1) * searchCondition.getPageSize());
            solrQuery.setRows(searchCondition.getPageSize());
            LogRecod.print("start query:");
            QueryResponse response = solrClient.query(solrQuery);
            SolrDocumentList documents = response.getResults();
            total = documents.getNumFound();
            for (SolrDocument document : documents) {
                Long pid = Long.parseLong(document.get("id").toString());
                Paper paper = paperService.selectByPaperId(pid);
                papers.add(paper);
            }
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        PageInfo<Paper> pageInfo = PageUtil.transferListToPageInfo(papers, total, 1, 10);
        return pageInfo;
    }

    private String parseSearchCondition(PaperSearchCondition searchCondition) {
        StringBuilder q = new StringBuilder();
        switch (searchCondition.getSearchType()) {
            case PaperConditionType.KEYWORD:
                q.append("paper_keyword:" + searchCondition.getQ());
                break;
            case PaperConditionType.SUMMARY:
                q.append("paper_summary:" + searchCondition.getQ());
                break;
            case PaperConditionType.TITLE:
                q.append("paper_title:" + searchCondition.getQ());
                break;
            default:
                break;
        }
        return q.toString();
    }
}
