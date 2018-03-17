package com.stephen.lab.controller.semantic;

import com.stephen.lab.model.semantic.DataSource;
import com.stephen.lab.service.semantic.DataSourceService;
import com.stephen.lab.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/**
 * Created by stephen on 2017/7/18.
 */
@Controller
@RequestMapping("/semantic")
public class SemanticIndexController {
    @Autowired
    private DataSourceService dataSourceService;

    @RequestMapping("/index")
    public String index() {
        return "index";
    }

    @RequestMapping("/source")
    @ResponseBody
    public Response getDateSource() {
        List<DataSource> sourceList = dataSourceService.getAllDataSources();
        return Response.success(sourceList);
    }
}
