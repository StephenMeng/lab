package com.stephen.lab.controller;

import com.github.pagehelper.Page;
import com.stephen.lab.model.DataSource;
import com.stephen.lab.service.DataSourceService;
import com.stephen.lab.service.PaperService;
import com.stephen.lab.util.Holder;
import com.stephen.lab.util.LogRecod;
import com.stephen.lab.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by stephen on 2017/7/18.
 */
@Controller
public class IndexController {
    @Autowired
    private DataSourceService dataSourceService;

    @RequestMapping("/")
    public ModelAndView index() {
        return new ModelAndView("index");
    }

    @RequestMapping("/source")
    @ResponseBody
    public Response getDateSource() {
        List<DataSource> sourceList=dataSourceService.getAllDataSources();
        return Response.success(sourceList);
    }


}
