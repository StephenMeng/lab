package com.stephen.lab.controller.crawler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.stephen.lab.constant.crawler.NstrsConstant;
import com.stephen.lab.model.crawler.Nstrs;
import com.stephen.lab.service.crawler.NstrsService;
import com.stephen.lab.util.HttpUtils;
import com.stephen.lab.util.LogRecod;
import com.stephen.lab.util.Response;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sun.rmi.runtime.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("nstrs")
public class NstrsController {
    @Autowired
    private NstrsService nstrsService;

    public static String api = "";

    public static String proxy = null;
    private static Boolean running = true;

    @RequestMapping("crawl")
    public Response crawl(@RequestParam("area") String area,
                          @RequestParam("pageNum") Integer pageNum) throws IOException {
        Map<String, Integer> areaMap = new HashMap<>();
        areaMap.put("北京", 1934);
        areaMap.put("吉林", 143);
        areaMap.put("上海", 710);
        areaMap.put("江苏", 521);
        areaMap.put("浙江", 333);
        areaMap.put("安徽", 165);
        areaMap.put("江西", 72);
        areaMap.put("山东", 280);
        areaMap.put("河南", 94);
        areaMap.put("湖北", 402);
        areaMap.put("湖南", 199);
        areaMap.put("湖北广西", 56);
        areaMap.put("海南", 19);
        areaMap.put("贵州", 39);
        areaMap.put("云南", 86);
        areaMap.put("青海", 14);
        areaMap.put("宁夏", 19);
        for (Map.Entry<String, Integer> m : areaMap.entrySet()) {
            area = m.getKey();
            pageNum = m.getValue();
//        List<Nstrs> nstrsList = nstrsService.selectAll();
            FileWriter fileWriter = new FileWriter(new File("c:/users/stephen/desktop/nstrs-error.txt"), true);
            Executor executor = Executors.newFixedThreadPool(1);
            for (int i = 1; i <= pageNum; i++) {
                Integer page = i;
//            executor.execute(() -> {
                Map<String, String> map = new HashMap<>();
                map.put("pageIndex", i + "");
                map.put("pageSize", "10");
                map.put("action", "anarea");
                map.put("jihua", "");
                map.put("fieldid", "");
                map.put("type", "");
                map.put("xueke", "");
                map.put("bumen", "");
                map.put("area", area);
                String url = "http://www.nstrs.cn/ashx/baogaoliulan.ashx";
                map.put("pageIndex", page + "");
                HttpResponse response = HttpUtils.httpPost(url, map);
                if (response == null) {
                    continue;
                }
                String result = null;
                try {
                    result = IOUtils.toString(response.getEntity().getContent());
                } catch (IOException e) {
                    fileWriter.write(area + "\t" + page + "\r\n");
                    e.printStackTrace();
                    continue;
                }
                Pattern pattern = Pattern.compile("href='(.*?)'");
                Matcher matcher = pattern.matcher(result);
                while (matcher.find()) {
                    String idUrl = matcher.group(1);
                    Nstrs nstrs = new Nstrs();
                    nstrs.setNstrsId(idUrl);
//                if (!nstrsList.contains(idUrl)) {
//                    nstrs.setProvince(area);
//                    nstrsService.addOrUpdate(nstrs);
//                }
                    Nstrs exist = nstrsService.selectOne(nstrs);
                    if (exist != null) {
                        nstrs.setProvince(area);
                        nstrsService.updateSelective(nstrs);
                    } else {
                        nstrs.setProvince(area);
                        nstrsService.add(nstrs);
                    }
                }
            }
            fileWriter.close();
        }
        return Response.success("");
    }

    @RequestMapping("crawl_detail")
    public Response crawl(@RequestParam("spiderId") String spiderId, @RequestParam("orderno") String orderno) throws IOException, InterruptedException {
        api = "http://api.xdaili.cn/xdaili-api//greatRecharge/getGreatIp?spiderId=" +
                spiderId + "&orderno=" + orderno + "&returnType=2&count=1";
        final Integer lock = 1;
        BlockingQueue<String> blockingQueue = new LinkedBlockingQueue<>();
        //代理IP获取线程
        new ProxyThread(running, blockingQueue, lock).start();
        List<Nstrs> nstrsList = nstrsService.selectAll();
//        ExecutorService service = Executors.newFixedThreadPool(5);
        ExecutorService service = Executors.newFixedThreadPool(50);

        Thread.sleep(5000);
        proxy = blockingQueue.take();
        for (int i = 0; i < nstrsList.size(); i++) {
            //NSTRS数据获取线程
            Nstrs nstrs = nstrsList.get(i);
            if (nstrs.getTitle() == null) {
//                LogRecod.print(nstrs);
                Future future = service.submit(
                        new FutureTask<Nstrs>(
                                new NstrsCrawlRunnable(running, proxy, nstrs, blockingQueue, lock)));
//                try {
//                    LogRecod.print(future.get());
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                }
            }
        }
        service.shutdown();
        return Response.success("");
    }


    class ProxyThread extends Thread {
        private Integer lock;
        private BlockingQueue<String> blockingQueue;

        public ProxyThread(Boolean r, BlockingQueue<String> blockingQueue, Integer lock) {
            this.lock = lock;
            this.blockingQueue = blockingQueue;
        }

        @Override
        public void run() {
            while (running) {
                LogRecod.print(blockingQueue.size());
                if (blockingQueue.size() < 1) {
//                if(false){
//                    api = "http://api.xdaili.cn/xdaili-api//greatRecharge/getGreatIp?spiderId=8eea027bb1f545d2bf7bd135448be78b&orderno=YZ20179142517TEmvok&returnType=2&count=1";
                    try {
                        HttpResponse response = HttpUtils.httpGet(api);
                        JSONObject jsonObject = JSONObject.parseObject(IOUtils.toString(response.getEntity().getContent()));
                        JSONArray result = jsonObject.getJSONArray("RESULT");
                        for (int i = 0; i < result.size(); i++) {
                            JSONObject j = result.getJSONObject(i);
                            String ip = j.getString("ip");
                            String port = j.getString("port");
                            LogRecod.print("get-------" + ip + ":" + port);
                            blockingQueue.add(ip + ":" + port);
                        }
                    } catch (IOException e) {
//                        running = false;
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class NstrsCrawlRunnable implements Callable<Nstrs> {
        private Nstrs nstrs;
        private BlockingQueue<String> blockingQueue;
        private Integer lock;
        private String p;

        public NstrsCrawlRunnable(Boolean running, String proxy, Nstrs nstrs, BlockingQueue<String> blockingQueue, Integer lock) {
            this.p = proxy;
            this.nstrs = nstrs;
            this.blockingQueue = blockingQueue;
            this.lock = lock;
        }

        public Nstrs call() {
            if (running) {
                p = proxy;
                String url = NstrsConstant.URL_PREFIX + "/" + nstrs.getNstrsId();
                try {
                    Nstrs n = getSingleNstrs(nstrs.getId(), url, proxy);
                    return n;
                } catch (Exception e) {
                    e.printStackTrace();
                    synchronized (lock) {
                        if (p.equals(proxy)) {
                            try {
                                proxy = blockingQueue.take();
                                LogRecod.print(proxy);
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                    return null;
                }
            }
            return null;
        }

    }

    private Nstrs getSingleNstrs(Integer id, String url, String proxy) throws IOException {
        HttpResponse response = HttpUtils.httpGet(url, null, proxy);
        String result = IOUtils.toString(response.getEntity().getContent());
        Document document = Jsoup.parse(result);
        Element title = null;
        title = document.select("span[id=lblTitle]").first();
        if (title == null) {
            throw new IOException();
        }
        Element titleEn = null;
        try {
            titleEn = document.select("span[id=EngTitle]").first();
        } catch (Exception e) {
        }
        Element openRange = null;
        try {
            openRange = document.select("span[id=FFWZ]").first();
        } catch (Exception e) {
        }
        Element date = null;
        try {
            date = document.select("span[id=spancreatetime]").first();
        } catch (Exception e) {
        }
        Element type = null;
        try {
            type = document.select("span[style=display: block; float: left; width: 255px;]").first();
        } catch (Exception e) {
        }
        Element author = null;
        try {
            author = document.select("tr:contains(报告作者)").first().select("label").first();
        } catch (Exception e) {
        }
        Element abstractCn = null;
        try {
            abstractCn = document.select("tr:contains(中文摘要)").first().select("label").first();
        } catch (Exception e) {
        }
        Element abstractEn = null;
        try {
            abstractEn = document.select("tr:contains(英文摘要)").first().select("label").first();
        } catch (Exception e) {
        }
        Element keywordCn = null;
        try {
            keywordCn = document.select("tr:contains(中文关键词)").first().select("label").first();
        } catch (Exception e) {
        }
        Element keywordEn = null;
        try {
            keywordEn = document.select("tr:contains(英文关键词)").first().select("label").first();
        } catch (Exception e) {
        }
        Element pageNum = null;
        try {
            pageNum = document.select("tr:contains(全文页数)").first().select("label").first();
        } catch (Exception e) {
        }
        Element lno = null;
        try {
            lno = document.select("tr:contains(馆 藏 号)").first().select("label").first();
        } catch (Exception e) {
        }
        Nstrs nstrs = new Nstrs();
        nstrs.setId(id);
        nstrs.setTitle(title == null ? null : title.text());
        nstrs.setTitleEn(titleEn == null ? null : titleEn.text());
        nstrs.setAbstractCn(abstractCn == null ? null : abstractCn.text());
        nstrs.setAbstractEn(abstractEn == null ? null : abstractEn.text());
        nstrs.setAuthor(author == null ? null : author.text());
        nstrs.setDate(date == null ? null : date.text().replaceAll("编制时间：", ""));
        nstrs.setKeywordCn(keywordCn == null ? null : keywordCn.text());
        nstrs.setKeywordEn(keywordEn == null ? null : keywordEn.text());
        nstrs.setLno(lno == null ? null : lno.text());
        nstrs.setType(type == null ? null : type.text().replaceAll("报告类型：", ""));
        nstrs.setPageNum(pageNum == null ? null : pageNum.text());
        nstrs.setOpenRange(openRange == null ? null : openRange.text().replaceAll("公开范围：", ""));
        LogRecod.print(nstrs);
        try {
            nstrsService.updateSelective(nstrs);
        } catch (Exception e) {
            LogRecod.print(e);
        }
        return nstrs;
    }

    private void getXICIProxyUrl() {
        //                    String proxyUrl = "http://www.xicidaili.com/nn/" + page;
//                    Map<String, String> map = new HashMap<>();
//                    map.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
//                    map.put("Accept-Encoding", "gzip, deflate");
//                    map.put("Accept-Language", "zh-CN,zh;q=0.8");
//                    map.put("Connection", "keep-alive");
//                    map.put("Cookie", "_free_proxy_session=BAh7B0kiD3Nlc3Npb25faWQGOgZFVEkiJTM2MzdkNTIyNGM1MzY3NDAyYThlNGRmZTVlYTNmYWUxBjsAVEkiEF9jc3JmX3Rva2VuBjsARkkiMWkyc2N5TmFGd3FlWEVoMzBocGFHU2FLMzdjMUFaNlE5eGZocUNwU0RlK009BjsARg%3D%3D--cc54da36a23eb32ee037523a1c0cd45e9ee4a579; Hm_lvt_0cf76c77469e965d2957f0553e6ecf59=1505144842; Hm_lpvt_0cf76c77469e965d2957f0553e6ecf59=1505222084");
//                    map.put("Host", "www.xicidaili.com");
//                    map.put("If-None-Match", "W/\"5569f44ee3f94995869358cfdfe4c899\"");
//                    map.put("Referer", "http://www.xicidaili.com/nn/2");
//                    map.put("Upgrade-Insecure-Requests", "1");
//                    map.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.113 Safari/537.36");
//                    HttpResponse response = null;
//                    try {
//                        response = HttpUtils.httpGet(proxyUrl, map);
//                        String result = IOUtils.toString(response.getEntity().getContent());
//                        Document document = Jsoup.parse(result);
//                        Elements elements = document.getElementsByTag("tr");
//                        for (Element element : elements) {
//                            if (element.html().contains("img")) {
//                                String host = element.select("td").get(1).text();
//                                String port = element.select("td").get(2).text();
//                                blockingQueue.addOrUpdate(host + ":" + port);
//                            }
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } finally {
//                        page++;
//                    }
    }
}
