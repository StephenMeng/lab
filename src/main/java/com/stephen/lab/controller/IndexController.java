package com.stephen.lab.controller;

import com.stephen.lab.util.LogRecod;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
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
        Map<String,Object>result=new HashMap<String, Object>();
        LogRecod.info(result);
        return  new ModelAndView("index",result);
    }
//    @RequestMapping("/error")
//    public ModelAndView error(){
//        Map<String,Object>result=new HashMap<String, Object>();
//        LogRecod.info(result);
//        return  new ModelAndView("error",result);
//    }
}
