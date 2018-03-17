package com.stephen.lab.controller.paper;

import com.stephen.lab.controller.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by stephen on 2018/3/16.
 */
@RequestMapping("/tag")
@Controller
public class TagIndexController extends BaseController {
    @RequestMapping("/index")
    public String index() {
        return "tag";
    }
}
