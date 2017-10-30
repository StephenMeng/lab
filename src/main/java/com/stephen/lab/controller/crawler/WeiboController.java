package com.stephen.lab.controller.crawler;

import com.stephen.lab.constant.crawler.ErrorConstant;
import com.stephen.lab.constant.crawler.UrlConstant;
import com.stephen.lab.model.crawler.CrawlError;
import com.stephen.lab.model.crawler.Weibo;
import com.stephen.lab.service.crawler.CrawlErrorService;
import com.stephen.lab.service.crawler.WeiboCrawlerService;
import com.stephen.lab.util.CodeUtils;
import com.stephen.lab.util.HttpUtils;
import com.stephen.lab.util.LogRecod;
import com.stephen.lab.util.paser.WeiboParser;
import com.stephen.lab.util.paser.WeiboSearchParser;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by stephen on 2017/10/29.
 */
@RestController
@RequestMapping("weibo")
public class WeiboController {
    @Autowired
    private WeiboCrawlerService weiboCrawlerService;
    @Autowired
    private CrawlErrorService crawlErrorService;

    @RequestMapping("user")
    public void crawlSimple(@RequestParam("scriptUri") String scriptUri,
                            @RequestParam("id") String id,
                            @RequestParam("hasU") boolean hasU,
                            @RequestParam("start") int page,
                            @RequestParam("end") int end) {
        scriptUri = normalizingScriptUri(scriptUri, hasU);
        for (int i = 1; i <= page; i++) {
            String cookie = "YF-V5-G0=fec5de0eebb24ef556f426c61e53833b; YF-Page-G0=416186e6974c7d5349e42861f3303251; _s_tentry=weibo.com; login_sid_t=03fc48e9875be68669566f810ae42a36; YF-Ugrow-G0=ea90f703b7694b74b62d38420b5273df; UOR=www.baidu.com,weibo.com,www.baidu.com; Apache=4855100182428.531.1509249179926; SINAGLOBAL=4855100182428.531.1509249179926; ULV=1509249179947:1:1:1:4855100182428.531.1509249179926:; WBtopGlobal_register_version=b81eb8e02b10d728; SUB=_2A2508SFgDeRhGeVN6lQQ9SvIyzmIHXVXhxWorDV8PUNbmtBeLXTVkW8qOoRg6_2VEltLwq1b_fWUwqkVcg..; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WhHG.930Gf5fOgXqM.y_wER5JpX5KzhUgL.Foe0eKqpSK-Xeh-2dJLoIEBLxKBLB.eL122LxK.L1hzLB-2LxKnLBK2LBozLxK-LBozL1K5t; SUHB=0rr7s9yZ9aav43; ALF=1540785327; SSOLoginState=1509249328; wb_cusLike_3316155405=N";
            List<Weibo> weiboList = new ArrayList<>();
            weiboList.addAll(getFirstPage(i, scriptUri, cookie));
            weiboList.addAll(getBarPage(i, id, scriptUri, cookie));
            LogRecod.print(weiboList.size());
            insertIntoDataBase(weiboList);
        }
    }

    @RequestMapping("user_cookie")
    public void crawlSimpleWithCookiewInfo(@RequestParam("scriptUri") String scriptUri,
                                           @RequestParam("id") String id,
                                           @RequestParam("hasU") boolean hasU,
                                           @RequestParam("start") int start,
                                           @RequestParam("end") int end,
                                           HttpServletRequest request) {
        scriptUri = normalizingScriptUri(scriptUri, hasU);
        String query = request.getQueryString();
        String cookie = query.substring(query.indexOf("=", query.indexOf("id")) + 1);
        for (int i = start; i <= end; i++) {
            List<Weibo> weiboList = new ArrayList<>();
            weiboList.addAll(getFirstPage(i, scriptUri, cookie));
            weiboList.addAll(getBarPage(i, id, scriptUri, cookie));
            insertIntoDataBase(weiboList);
        }
    }

    private String normalizingScriptUri(String script_uri, boolean hasU) {
        if (hasU) {
            return "/u/" + script_uri;
        } else {
            return "/" + script_uri;
        }
    }

    private List<Weibo> getBarPage(int i, String id, String script_uri, String cookie) {
        List<Weibo> weiboList = new ArrayList<>();
        for (int k = 0; k < 2; k++) {
            String url = UrlConstant.WEI_BO + "/p/aj/v6/mblog/" +
                    "mbloglist?ajwvr=6&domain=100206&is_search=0&visible=0&is_all=1&" +
                    "is_tag=0&profile_ftype=1&page=" + i +
                    "&pagebar=" + k +
                    "&pl_name=Pl_Official_MyProfileFeed__27&id=" + id +
                    "&script_uri=" + script_uri +
                    "&feed_type=0&pre_page=" + i +
                    "&domain_op=100206&__rnd=1509255368651";
            Map<String, String> headers = new HashMap<>();
            headers.put("Cookie", cookie);
            weiboList.addAll(getBarPageWeibos(url, headers));
        }
        return weiboList;
    }

    private List<Weibo> getBarPageWeibos(String url, Map<String, String> headers) {
        List<Weibo> weiboList = new ArrayList<>();
        try {
            HttpResponse response = HttpUtils.httpGet(url, headers);
            String html = IOUtils.toString(response.getEntity().getContent(), "gbk");
            html = normalizingBarPage(html);
            weiboList.addAll(new WeiboParser().parse(html));
        } catch (Exception e) {
            insetErrorInfoIntoDataBase(url, e);
            e.printStackTrace();
        }
        return weiboList;
    }

    private String normalizingBarPage(String html) {
        String htmlStr = CodeUtils.unicode2String(html);
        htmlStr = htmlStr.substring(htmlStr.indexOf("<div"), htmlStr.lastIndexOf("div>") + 4);
        htmlStr = htmlStr.replaceAll("\"", "");
        htmlStr = htmlStr.replaceAll("\\\\", "");
        return htmlStr;
    }

    private List<Weibo> getFirstPage(int i, String script_uri, String cookie) {
        String url = UrlConstant.WEI_BO + script_uri + "/" +
                "?is_search=0&visible=0" +
                "&is_all=1&is_tag=0&profile_ftype=1&page=" + i;
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", cookie);
        return getFirstPageWeibos(url, headers);
    }

    private List<Weibo> getFirstPageWeibos(String url, Map<String, String> headers) {
        try {
            HttpResponse response = HttpUtils.httpGet(url, headers);
            String html = IOUtils.toString(response.getEntity().getContent(), "utf-8");
            html = normalizingFirstPage(html);
            List<Weibo> weiboList = new WeiboParser().parse(html);
            return weiboList;
        } catch (Exception e) {
            insetErrorInfoIntoDataBase(url, e);
        }
        return new ArrayList<>();
    }

    private String normalizingFirstPage(String html) {
        int startIndex = html.lastIndexOf("Pl_Official_MyProfileFeed__27");
        int endIndex = html.indexOf("/script", startIndex);
        html = html.substring(startIndex, endIndex);
        String htmlStr = html.substring(html.indexOf("<div"), html.lastIndexOf("div>") + 4);
        htmlStr = htmlStr.replaceAll("\"", "");
        htmlStr = htmlStr.replaceAll("\\\\", "");
        return htmlStr;
    }

    @RequestMapping("search")
    public void search(@RequestParam("url") String url,
                       @RequestParam("start") int start,
                       @RequestParam("end") int end) {
        String cookie = "YF-V5-G0=fec5de0eebb24ef556f426c61e53833b; YF-Page-G0=416186e6974c7d5349e42861f3303251; _s_tentry=weibo.com; login_sid_t=03fc48e9875be68669566f810ae42a36; YF-Ugrow-G0=ea90f703b7694b74b62d38420b5273df; UOR=www.baidu.com,weibo.com,www.baidu.com; Apache=4855100182428.531.1509249179926; SINAGLOBAL=4855100182428.531.1509249179926; ULV=1509249179947:1:1:1:4855100182428.531.1509249179926:; WBtopGlobal_register_version=b81eb8e02b10d728; SUB=_2A2508SFgDeRhGeVN6lQQ9SvIyzmIHXVXhxWorDV8PUNbmtBeLXTVkW8qOoRg6_2VEltLwq1b_fWUwqkVcg..; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WhHG.930Gf5fOgXqM.y_wER5JpX5KzhUgL.Foe0eKqpSK-Xeh-2dJLoIEBLxKBLB.eL122LxK.L1hzLB-2LxKnLBK2LBozLxK-LBozL1K5t; SUHB=0rr7s9yZ9aav43; ALF=1540785327; SSOLoginState=1509249328; wb_cusLike_3316155405=N";
        for (int i = start; i <= end; i++) {
            String pageUrl = getPageUrl(url, i);
            Map<String, String> headers = new HashMap<>();
            headers.put("Cookie", cookie);
            List<Weibo> weiboList = getSearchPageWeibos(pageUrl, headers);
            LogRecod.print(weiboList);
            insertIntoDataBase(weiboList);
        }
    }

    private String getPageUrl(String url, int page) {
        if (!url.contains("page=")) {
            return "http://s.weibo.com/weibo/" + url + "page=" + page;
        }
        String before = url.substring(0, url.indexOf("page="));
        String after = url.substring(url.indexOf("&", url.indexOf("page=")) + 1);
        return "http://s.weibo.com/weibo/" + before + after + "page=" + page;
    }

    @RequestMapping("search_cookie")
    public void searchWithCookie(@RequestParam("url") String url,
                                 @RequestParam("start") int start,
                                 @RequestParam("end") int end,
                                 HttpServletRequest request) {
        String query = request.getQueryString();
        String cookie = query.substring(query.indexOf("=", query.indexOf("end")) + 1);
        for (int i = start; i <= end; i++) {
            String pageUrl = getPageUrl(url, i);
            Map<String, String> headers = new HashMap<>();
            headers.put("Cookie", cookie);
            List<Weibo> weiboList = getSearchPageWeibos(pageUrl, headers);
            insertIntoDataBase(weiboList);
        }
    }

    private void insertIntoDataBase(List<Weibo> weiboList) {
        weiboList.forEach(weiboCrawlerService::addOrUpdate);
    }

    private List<Weibo> getSearchPageWeibos(String url, Map<String, String> headers) {
        try {
            HttpResponse response = HttpUtils.httpGet(url, headers);
            String html = IOUtils.toString(response.getEntity().getContent(), "utf-8");
            html = normalizingSearchPage(html);
            List<Weibo> weiboList = new WeiboSearchParser().parse(html);
            return weiboList;
        } catch (Exception e) {
            insetErrorInfoIntoDataBase(url, e);
        }
        return new ArrayList<>();
    }

    private String normalizingSearchPage(String html) {
        html = CodeUtils.unicode2String(html);
        int startIndex = html.lastIndexOf("pl_weibo_direct");
        int endIndex = html.indexOf("/script", startIndex);
        html = html.substring(startIndex, endIndex);
        String htmlStr = html.substring(html.indexOf("<div"), html.lastIndexOf("div>") + 4);
//        htmlStr = htmlStr.replaceAll("\"", "");
        htmlStr = htmlStr.replaceAll("\\\\", "");
        return htmlStr;
    }

    private void insetErrorInfoIntoDataBase(String url, Exception e) {
        CrawlError error = new CrawlError();
        error.setStatus(ErrorConstant.ERROR);
        error.setSourceId(UrlConstant.WEI_BO_ID);
        error.setErrorHref(url);
        error.setCreateDate(new Date());
        crawlErrorService.addErrorItem(error);
        e.printStackTrace();
    }
}
