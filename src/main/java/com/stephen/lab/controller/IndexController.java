package com.stephen.lab.controller;

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
import java.util.Map;

/**
 * Created by stephen on 2017/7/18.
 */
@Controller
public class IndexController {
    @Autowired
    private PaperService paperService;

    @RequestMapping("/")
    public ModelAndView index() {
        return new ModelAndView("index");
    }

    @RequestMapping("/index")
    @ResponseBody
    public Response indexPage() {
        return Response.success(paperService.index());
    }

}
