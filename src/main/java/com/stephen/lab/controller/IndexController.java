package com.stephen.lab.controller;

import com.stephen.lab.util.Holder;
import com.stephen.lab.util.LogRecod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by stephen on 2017/7/18.
 */
@Controller
public class IndexController {

    @RequestMapping("/")
    public ModelAndView index(){
        return  new ModelAndView("index");
    }
    @RequestMapping("/index")
    public ModelAndView indexPage(){
        return  new ModelAndView("index");
    }
}
