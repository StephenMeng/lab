package com.stephen.lab.controller;

import com.stephen.lab.constant.LoginConstant;
import com.stephen.lab.constant.semantic.ResultEnum;
import com.stephen.lab.dto.UserDto;
import com.stephen.lab.model.User;
import com.stephen.lab.service.UserService;
import com.stephen.lab.util.Response;
import com.stephen.lab.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Controller
public class LoginController {
    @Autowired
    private UserService userService;

    @RequestMapping("toLogin")
    public ModelAndView toLogin(String userName, String password, HttpServletRequest request) {
        return new ModelAndView("login");
    }

    @RequestMapping("login")
    @ResponseBody
    public Response login(@RequestParam("userName") String inputUserName,
                          @RequestParam("password") String inputPassword,
                          HttpServletRequest request) {
        User user = userService.check(inputUserName, inputPassword);
        if (user == null) {
            return Response.error(ResultEnum.FAIL_PARAM_WRONG.getCode(), "username or password is wrong", "用户名或密码错误");
        }
        UserDto userDto = user.modelToDto();
        request.getSession().setAttribute(LoginConstant.USER, userDto);
        return Response.success(userDto);
    }

    @RequestMapping("logout")
    @ResponseBody
    public Response logout(HttpServletRequest request) {
        request.getSession().invalidate();
        return null;
    }
}
