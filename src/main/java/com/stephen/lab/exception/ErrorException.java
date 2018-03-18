package com.stephen.lab.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by stephen on 2018/3/16.
 */
//@ControllerAdvice

public class ErrorException {

//    @ExceptionHandler(value = Exception.class)
//    public ModelAndView defaultErrorHandler(HttpServletRequest req, Exception e) {
//        ModelAndView mv = new ModelAndView();
//        mv.addObject("e", e);
//        mv.addObject("uri", req.getRequestURI());
//        return mv;
//    }
}
