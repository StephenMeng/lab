package com.stephen.lab.controller.paper;

import com.stephen.lab.util.Progress.Progress;
import com.stephen.lab.util.Response;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by stephen on 2018/3/16.
 */
@RequestMapping("/tag")
@Controller
public class TagIndexController {
    @RequestMapping("")
    public ModelAndView index() {
        return new ModelAndView("tag");
    }
}
