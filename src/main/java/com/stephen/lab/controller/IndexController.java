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
@RestController
public class IndexController {
    @RequestMapping("/")
    public ModelAndView index(){
        Map<String,Object>result=new HashMap<String, Object>();
        LogRecod.info(result);
        LogRecod.print(Holder.getUser());
        return  new ModelAndView("detail",result);
    }
//    @RequestMapping("/error")
//    public ModelAndView error(){
//        Map<String,Object>result=new HashMap<String, Object>();
//        LogRecod.info(result);
//        return  new ModelAndView("error",result);
//    }
}
