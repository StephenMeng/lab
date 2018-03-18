package com.stephen.lab.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {
    @RequestMapping("")
    public ModelAndView index() {
        return new ModelAndView("index");
    }

    @RequestMapping("/")
    public ModelAndView index2() {
        return new ModelAndView("index");
    }
}
