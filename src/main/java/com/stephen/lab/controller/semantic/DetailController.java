package com.stephen.lab.controller.semantic;

import com.stephen.lab.dto.semantic.PaperDto;
import com.stephen.lab.model.semantic.Paper;
import com.stephen.lab.service.semantic.PaperService;
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
@RequestMapping("semantic/detail")
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
        return Response.success(PaperDto.toDto(paper));
    }

}
