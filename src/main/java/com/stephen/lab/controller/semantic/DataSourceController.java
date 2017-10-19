package com.stephen.lab.controller.semantic;

import com.stephen.lab.model.semantic.DataSource;
import com.stephen.lab.service.semantic.DataSourceService;
import com.stephen.lab.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("semantic/data-source")
public class DataSourceController {
    @Autowired
    private DataSourceService dataSourceService;

    @RequestMapping("/get")
    @ResponseBody
    public Response getDataSource(
    ) {
        List<DataSource> dataSources = dataSourceService.getAllDataSources();
        return Response.success(dataSources);
    }
}
