package com.stephen.lab.controller.semantic;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.stephen.lab.constant.semantic.EntityRelationType;
import com.stephen.lab.constant.semantic.PaperType;
import com.stephen.lab.constant.semantic.ResultEnum;
import com.stephen.lab.constant.semantic.SourceType;
import com.stephen.lab.dto.semantic.EntityGenIdDto;
import com.stephen.lab.dto.semantic.EntityJsonDto;
import com.stephen.lab.model.semantic.Entity;
import com.stephen.lab.model.semantic.Paper;
import com.stephen.lab.service.semantic.EntityService;
import com.stephen.lab.service.semantic.PaperService;
import com.stephen.lab.util.ListUtil;
import com.stephen.lab.util.LogRecod;
import com.stephen.lab.util.RandomIDUtil;
import com.stephen.lab.util.Response;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("semantic/init")
public class InitController {

    @Autowired
    private PaperService paperService;
    @Autowired
    private EntityService entityService;

    @RequestMapping("scdn")
    @ResponseBody
    public void insertCsdn() throws IOException {
        HttpClientBuilder clientBuilder = HttpClients.custom().setConnectionManagerShared(true);
        CloseableHttpClient httpClient = clientBuilder.build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(5000).setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000).build();

        int count = 0;
        StringBuilder entityStringBuilder = null;
        String keyword = "%E4%BA%91%E8%AE%A1%E7%AE%97";//云计算
//        String keyword = "%E6%95%B0%E6%8D%AE%E6%8C%96%E6%8E%98";//数据挖掘

        for (int i = 1; i < 1; i++) {
            String s = "http://so.csdn.net/so/search/s.do?" +
                    "p=" + i + "&q=" + keyword + "&t=blog&domain=&o=&s=&u=null&l=null&f=null";
            LogRecod.print(s);

            try {
                HttpGet get = new HttpGet(s);
                get.setConfig(requestConfig);
                CloseableHttpResponse httpResponse = httpClient.execute(get);
                InputStreamReader inputStreamReader = null;
                BufferedReader bufferedReader = null;

                HttpEntity entity = httpResponse.getEntity();
                entityStringBuilder = new StringBuilder();
                if (null != entity) {
                    inputStreamReader = new InputStreamReader(httpResponse.getEntity().getContent(), "utf-8");
                    bufferedReader = new BufferedReader(inputStreamReader, 8192);
                    try {
                        String line = null;
                        while ((line = bufferedReader.readLine()) != null) {
                            entityStringBuilder.append(line + "\n");
                        }
                        Document document = Jsoup.parse(entityStringBuilder.toString());
                        Elements elements = document.select("dl[class=search-list J_search]");
                        for (Element element : elements) {
                            Paper paper = new Paper();

                            String title = element.select("dt").first().text();
                            Element authorTime = element.select("dd[class=author-time]").first();
                            if (authorTime != null) {
                                paper.setAuthor(authorTime.select("a").first().text());
                                paper.setAuthorUrl(authorTime.select("a").first().attr("href"));
                                String atStr = authorTime.text();
                                paper.setPubDate(atStr.substring(atStr.indexOf("日期：") + 3, atStr.indexOf("浏览")).replaceAll(" ", ""));
                                paper.setReadNum(Integer.parseInt(atStr.substring(atStr.indexOf("浏览") + 2, atStr.indexOf("次"))
                                        .replaceAll(" ", "").trim()));
                            }
                            String abs = element.select("dd[class=search-detail]").first().text();
                            String link = element.select("dd[class=search-link]").first().text();
                            paper.setTitle(title);
                            paper.setSummary(abs);
                            paper.setPaperUrl(link);
                            paper.setSource(SourceType.CSDN);
                            paper.setPaperType(PaperType.BLOG.getTypeCode());
                            paper.setPaperLinkId(RandomIDUtil.randomID(16));
                            paperService.addPaper(paper);
                            LogRecod.print(paper);
                            count++;
                            LogRecod.print(count);
                        }
                    } catch (Exception e) {
                        LogRecod.print(e);
                    } finally {
                        try {
                            if (inputStreamReader != null) {
                                inputStreamReader.close();
                            }
                        } catch (Exception e) {
                            LogRecod.print(e);
                        }
                        try {
                            if (bufferedReader != null) {
                                bufferedReader.close();
                            }
                        } catch (Exception e) {
                            LogRecod.print(e);
                        }
                        try {
                            if (httpResponse != null) {
                                httpResponse.close();
                            }
                        } catch (Exception e) {
                            LogRecod.print(e);
                        }
                    }
                }
            } catch (Exception e) {
                LogRecod.print(e);
            }
        }
    }

    @RequestMapping("cnki")
    @ResponseBody
    public void inputCnkiData() throws IOException {
        File dir = new File("C:\\Users\\stephen\\Desktop\\data");
        for (File file : dir.listFiles()) {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), "utf-8")
            );
            String line = null;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\r\n");
                if (line.equals("")) {
                    parseCnkiStr(stringBuilder.toString());
                    stringBuilder = new StringBuilder();
                }
            }
//            break;
        }
    }

    private void parseCnkiStr(String s) {
        if (s.equals("")) {
            return;
        }
        try {
//            LogRecod.print(s);
            Paper paper = new Paper();
            String[] items = s.split("\r\n");
            if (items == null || items.length < 1) {
                return;
            }
            String tilu = items[0];
            String issbnLine = null;
            String keywordLine = null;
            String orgLine = null;
            String abLine = null;
            for (String line : items) {
                if (line.startsWith("ISSN")) {
                    issbnLine = line;
                }
                if (line.startsWith("关键词")) {
                    keywordLine = line;
                }
                if (line.startsWith("机构")) {
                    orgLine = line;
                }
                if (line.startsWith("摘要")) {
                    abLine = line;
                }
            }
            String authors = tilu.substring(s.indexOf("]") + 1, s.indexOf(".")).replaceAll(",", ";").trim();
            String left = tilu.substring(s.indexOf(".") + 1);
            String title = left.substring(0, left.indexOf("["));
            String type = left.substring(left.indexOf("["), left.indexOf("]"));
            if (type.toLowerCase().equals("j")) {
                type = "1";
            } else {
                type = "99";
            }
            left = left.substring(left.indexOf(".") + 1);
            String journal = left.substring(0, left.indexOf(","));
            left = left.substring(left.indexOf(",") + 1);
            String year = left.substring(0, left.indexOf(","));
            left = left.substring(left.indexOf(","));

            String issn = null;
            if (issbnLine != null) {
                issn = issbnLine.substring(5);
            }
            String keyword = null;
            String classify = null;
            if (keywordLine.contains("中图法分类号")) {
                keyword = keywordLine.substring(4, keywordLine.indexOf("中图"));
                classify = keywordLine.substring(keywordLine.indexOf("中图法分类号") + 6).trim();
            } else {
                keyword = keywordLine.substring(4);
            }
            paper.setPaperType(Integer.parseInt(type));
            paper.setSource(SourceType.CNKI);
            paper.setJournal(journal);
            paper.setTitle(title);
            if (year.length() <= 4) {
                paper.setYear(year);
            }
            paper.setAuthor(authors);
            paper.setKeyword(keyword);
            paper.setClassifyCn1(classify);
            if (issn != null) {
                paper.setIssn(issn);
            }
            if (orgLine != null) {
                paper.setOrgan(orgLine.substring(3).replaceAll(",", ";"));
            }
            if (abLine != null) {
                paper.setSummary(abLine.substring(3));
            }
            paper.setPaperLinkId(RandomIDUtil.randomID(16));
            paperService.addPaper(paper);
//            LogRecod.print(paper);
        } catch (Exception e) {
            LogRecod.print(e.getMessage());
        }
    }

    @RequestMapping("cssci")
    @ResponseBody
    public void inputCssciData() throws IOException {
        File dir = new File("C:\\Users\\stephen\\Desktop\\crawler\\csscidetail");
        List<String> libJournals = new ArrayList<>();
        libJournals.add("大学图书馆学报");
        libJournals.add("档案学通讯");
        libJournals.add("档案学研究");
        libJournals.add("国家图书馆学刊");
        libJournals.add("情报科学");
        libJournals.add("情报理论与实践");
        libJournals.add("情报学报");
        libJournals.add("情报杂志");
        libJournals.add("情报资料工作");
        libJournals.add("图书馆");
        libJournals.add("中国图书馆学报");
        libJournals.add("现代图书情报技术");
        libJournals.add("图书与情报");
        libJournals.add("图书情报知识");
        libJournals.add("图书情报工作");
        libJournals.add("图书馆杂志");
        libJournals.add("图书馆学研究");
        libJournals.add("图书馆论坛");
        libJournals.add("图书馆建设");
        libJournals.add("图书馆工作与研究");

        LogRecod.print("--开始存储到数据库--");
        for (File childDir : dir.listFiles()) {
            if (childDir.isDirectory()) {
                for (File file : childDir.listFiles()) {
                    for (String jn : libJournals) {
                        if (file.getName().equals(jn + ".txt")) {
                            BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(new FileInputStream(file), "gbk")
                            );
                            String line = null;
                            while ((line = reader.readLine()) != null) {
                                String[] items = line.split("\t");
                                Paper paper = new Paper();
                                paper.setPaperLinkId(RandomIDUtil.randomID(16));
                                paper.setSource(SourceType.CSSCI);
                                paper.setSourcePaperId(items[1]);
                                paper.setTitle(items[2]);
                                paper.setTitleEn(items[4]);
                                paper.setAuthor(items[5].replace("aaa", ";"));
                                paper.setOrgan(items[6].replace("aaa", ""));
                                paper.setPaperType(Integer.parseInt(items[8]));
                                paper.setClassifyCn1(items[10]);
                                paper.setClassifyCn2(items[11]);
                                paper.setKeyword(items[14].replace("aaa", ""));
                                paper.setYear(items[25]);
                                paper.setJournal(items[28]);
                                paperService.addPaper(paper);
                            }
                        }
                    }
                }
            }
        }
        LogRecod.print("--存储完毕--");
    }

    @RequestMapping(value = "upload", method = RequestMethod.POST)
    @ResponseBody
    public Response insertSemanticNet(MultipartHttpServletRequest multipartHttpServletRequest) throws IOException {
        String filename = multipartHttpServletRequest.getFile("file").getOriginalFilename();
        int result = -2;
        if (filename.endsWith("owl")) {
            result = insertIntoDatabase(multipartHttpServletRequest.getFile("file").getInputStream());
        }
        if (result == 0) {
            return Response.success("ok");
        } else if (result == -1) {
            return Response.error(ResultEnum.FAIL_PARAM_WRONG.getCode(), "file parses failed", "文件解析出错");
        } else {
            return Response.error(ResultEnum.FAIL_PARAM_WRONG.getCode(), "not correct file type", "文件格式错误,请上传owl格式文件");
        }
    }

    @RequestMapping("semantic")
    @ResponseBody
    public void insertSemanticNet() throws IOException {
        File fn = new File("C:\\Users\\stephen\\Desktop\\json.owl");
        InputStream inputStream = new FileInputStream(fn);
        insertIntoDatabase(inputStream);
    }

    private int insertIntoDatabase(InputStream fn) throws IOException {
        LogRecod.print("--开始存储semantic到数据库--");
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(fn, "utf-8"));
        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            line = line.replaceAll("http.*?#", "");
            stringBuilder.append(line);
        }
//        LogRecod.print(stringBuilder.toString());

        int count = 0;
        try {
            parseSemanticJson(stringBuilder.toString());
        } catch (Exception e) {
            count = -1;
        }
        return count;
    }

    private void parseSemanticJson(String s) {
        s = s.replaceAll("@id", "id");
        s = s.replaceAll("@type", "type");
        s = s.replaceAll("@value", "value");

        s = removeHttpTags(s);

        JSONArray jsonArray = JSONObject.parseArray(s);
        List<EntityGenIdDto> genIdDtos = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
//            LogRecod.print(jsonObject);
            EntityJsonDto entityJsonDto = JSONObject.parseObject(jsonObject.toJSONString(), EntityJsonDto.class);
//            LogRecod.print(entityJsonDto);
            if (entityJsonDto.getId().contains("genid")) {
                EntityGenIdDto genIdDto = new EntityGenIdDto();
                genIdDto.setGenId(entityJsonDto.getId());
                if (entityJsonDto.getOnProperty() != null) {
                    String relationStr = entityJsonDto.getOnProperty().get(0);
                    JSONObject rjo = JSONObject.parseObject(relationStr);
                    String relation = rjo.getString("id");
                    Long relationId = entityService.searchOrAddRelation(relation);
                    String entityBStr = entityJsonDto.getSomeValuesFrom().get(0);
                    JSONObject ejo = JSONObject.parseObject(entityBStr);
                    String entity = ejo.getString("id");

                    genIdDto.setRelationId(relationId);
                    genIdDto.setRelation(relation);
                    genIdDto.setExt(entity);
//                    LogRecod.print(genIdDto);
                    genIdDtos.add(genIdDto);
                }
            }
        }
//        LogRecod.print(genIdDtos);

        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            LogRecod.print(jsonObject);
            EntityJsonDto entityJsonDto = JSONObject.parseObject(jsonObject.toJSONString(), EntityJsonDto.class);
            if (!entityJsonDto.getId().contains("genid")) {
                Entity entity = new Entity();
                entity.setEntityName(entityJsonDto.getId());
                entity.setEntityType(ListUtil.ListToString(entityJsonDto.getType(), ";"));
                Entity exit = entityService.getEntity(entityJsonDto.getId());
                Long entityId = null;
                if (exit == null) {
//                    entity.setEntityType(entityJsonDto.getType());
                    entityId = entityService.addEntity(entityJsonDto.getId(), entityJsonDto.getType());
                    LogRecod.print(entityId);
                } else {
                    entityId = exit.getEntityId();
                }
                LogRecod.print(entityId);
//                exit.setEntityType(entity.getEntityType() + ListUtil.ListToString(entityJsonDto.getType(), ";"));
                if (entityJsonDto.getComment() != null) {
                    entityService.addEntityMap(entityId, entityJsonDto.getComment(), EntityRelationType.COMMENT.getName(), false, genIdDtos);
                }
                if (entityJsonDto.getEquivalentClass() != null) {
                    entityService.addEntityMap(entityId, entityJsonDto.getEquivalentClass(), EntityRelationType.EQUIVALENT_CLASS.getName(), true, genIdDtos);
                }
                if (entityJsonDto.getSubClassOf() != null) {
                    entityService.addEntityMap(entityId, entityJsonDto.getSubClassOf(), EntityRelationType.SUB_CLASS_OF.getName(), true, genIdDtos);
                }
            }
        }
    }

    private String removeHttpTags(String s) {
        s = s.replaceAll("http.*?^(\").*?#", "");
        return s;
    }
}
