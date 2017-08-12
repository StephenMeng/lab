package com.stephen.lab.controller;

import com.stephen.lab.model.Paper;
import com.stephen.lab.service.PaperService;
import com.stephen.lab.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by stephen on 2017/7/18.
 */
@Controller
@RequestMapping("detail")
public class DetailController {
    @Autowired
    private PaperService paperService;

    @RequestMapping(value = "")
    public ModelAndView detail() {
        return new ModelAndView("detail");
    }

    @RequestMapping("paper")
    @ResponseBody
    public Response getPaperContent(@RequestParam("paperId") Long paperId) {
        Paper paper = paperService.selectByPaperId(paperId);
        return Response.success(paper);
    }

}
