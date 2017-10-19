package com.stephen.lab.service.semantic.impl;

import com.github.pagehelper.PageInfo;
import com.stephen.lab.constant.semantic.PaperConditionType;
import com.stephen.lab.dto.semantic.PaperDto;
import com.stephen.lab.model.semantic.DataSource;
import com.stephen.lab.model.semantic.Paper;
import com.stephen.lab.model.condition.PaperSearchCondition;
import com.stephen.lab.service.semantic.PaperSearchService;
import com.stephen.lab.service.semantic.PaperService;
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
    @Autowired
    private DataSourceServiceImpl dataSourceService;

    @Override
    public PageInfo<PaperDto> searchPaper(PaperSearchCondition searchCondition) {
        List<PaperDto> papers = new ArrayList<>();
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
                PaperDto paperDto = PaperDto.toDto(paper);

                DataSource source = dataSourceService.getDataSource(paperDto.getSource());
                paperDto.setSourceUrl(source.getSourceUrl());
                paperDto.setSourceName(source.getSourceName());

                papers.add(paperDto);
            }
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        PageInfo<PaperDto> pageInfo = PageUtil.transferListToPageInfo(papers, total, searchCondition.getPageNo(), searchCondition.getPageSize());
        return pageInfo;
    }

    private String parseSearchCondition(PaperSearchCondition searchCondition) {
        StringBuilder q = new StringBuilder();
        q.append("(");
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
        q.append(")");
        if (searchCondition.getSources() != -1) {
            q.append(" && (");
            q.append("paper_source:" + searchCondition.getSources());
            q.append(")");
        }
        return q.toString();
    }
}
