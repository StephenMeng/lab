package com.stephen.lab.controller;

import com.stephen.lab.util.jedis.JedisAdapter;
import com.stephen.lab.util.Response;
import com.stephen.lab.model.User;
import com.stephen.lab.service.UserService;
import com.stephen.lab.util.solr.SolrAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Random;

/**
 * Created by stephen on 2017/7/15.
 */
@Controller
@RequestMapping("user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JedisAdapter jedisAdapter;
    @Autowired
    private SolrAdapter solrAdapter;
    @RequestMapping("get")
    @ResponseBody
    public Response getUser() {
        User user = userService.getUser(1);
        return Response.success(user);
    }

    @RequestMapping("getCache")
    @ResponseBody
    public Response getCache() {

        return Response.success(jedisAdapter.get("id"));
    }

    @RequestMapping("setCache")
    @ResponseBody
    public Response setCache() {
        return Response.success(jedisAdapter.set("id", new Random().nextInt(100)+""));
    }
    @RequestMapping("getSolr")
    @ResponseBody
    public Response getSolr(String s) {
        solrAdapter.querySolr(s);
        return Response.success(true);
    }
    @RequestMapping("setSolr")
    @ResponseBody
    public Response setSolr(String set) {
        solrAdapter.addDoc(set);
        return Response.success(true);
    }
}

