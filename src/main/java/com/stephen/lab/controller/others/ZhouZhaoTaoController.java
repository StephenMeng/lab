package com.stephen.lab.controller.others;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.stephen.lab.model.paper.KivaSimple;
import com.stephen.lab.util.MapUtils;
import com.stephen.lab.util.Response;
import com.stephen.lab.util.StringUtils;
import com.stephen.lab.util.nlp.lda.sample.com.FileUtil;
import com.stephen.lab.util.nlp.lda.sample.conf.ConstantConfig;
import com.stephen.lab.util.nlp.lda.sample.conf.PathConfig;
import com.stephen.lab.util.nlp.lda.sample.main.Documents;
import com.stephen.lab.util.nlp.lda.sample.main.LdaGibbsSampling;
import com.stephen.lab.util.nlp.lda.sample.main.LdaModel;
import io.swagger.models.auth.In;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.stephen.lab.util.nlp.lda.sample.main.LdaGibbsSampling.getParametersFromFile;

/**
 * Created by stephen on 2018/3/15.
 */
@RestController
@RequestMapping("zzt")
public class ZhouZhaoTaoController {
    private String testFilePath = "C:\\Users\\Stephen\\Desktop\\lda\\test.txt";
    private String yearData = "C:\\Users\\Stephen\\Desktop\\lda\\year.txt";

    @RequestMapping("high-freq")
    public Response highFreq() throws IOException {
        String fileInputPath = "C:\\Users\\stephen\\Desktop\\high-freq.txt";
        String fileOutputPath = "C:\\Users\\stephen\\Desktop\\high-freq-result.txt";

        List<String> stringList = Files.readLines(new File(fileInputPath), Charsets.UTF_8);
        Map<String, Integer> wordFreqMap = new HashMap<>();
        stringList.forEach(s -> {
            List<String> strings = Splitter.on(";").trimResults().splitToList(s);
            strings.forEach(string -> {
                if (!StringUtils.isNull(string)) {
                    if (wordFreqMap.containsKey(string.toLowerCase())) {
                        wordFreqMap.put(string.toLowerCase(), wordFreqMap.get(string.toLowerCase()) + 1);
                    } else {
                        wordFreqMap.put(string.toLowerCase(), 1);
                    }
                }
            });
        });
        File outF = new File(fileOutputPath);
        Map<String, Integer> result = MapUtils.sortMapByValue(wordFreqMap, true);
        for (Map.Entry<String, Integer> map : result.entrySet()) {
            Files.append(map.getKey() + "\t" + map.getValue() + "\r\n", outF, Charsets.UTF_8);
        }
        return Response.success("");
    }

    @RequestMapping("lda")
    public Response ldaTest() throws IOException {
        String testData = testFilePath;
        String resultPath = PathConfig.LdaResultsPath;
        String parameterFile = ConstantConfig.LDAPARAMETERFILE;

        LdaGibbsSampling.modelparameters ldaparameters = new LdaGibbsSampling.modelparameters();
        getParametersFromFile(ldaparameters, parameterFile);

        List<String> contents = readData(testData);
        Documents docSet = new Documents();
        docSet.readDocs(contents, ";");
        System.out.println("wordMap size " + docSet.termToIndexMap.size());
        FileUtil.mkdir(new File(resultPath));
        LdaModel model = new LdaModel(ldaparameters);
        System.out.println("1 Initialize the model ...");
        model.initializeModel(docSet);
        System.out.println("2 Learning and Saving the model ...");
        model.inferenceModel(docSet);
        System.out.println("3 Output the final model ...");
        model.saveIteratedModel(ldaparameters.iteration, docSet);
        System.out.println("Done!");
        return Response.success(true);
    }

    private List<String> readData(String testData) throws IOException {
        List<String> stringList = Files.readLines(new File(testData), Charsets.UTF_8);
        List<String> contents = new ArrayList<>();
        List<Integer> yearList = new ArrayList<>();
        for (String keywords : stringList) {
            try {
                String[] strings = keywords.split("\t");
                String kw = strings[0];
                Integer year = Integer.parseInt(strings[1]);
                int index = yearList.indexOf(year);
                if (index != -1) {
                    String tmp = contents.get(index) + kw;
                    contents.set(index, tmp);
                } else {
                    yearList.add(year);
                    contents.add(kw);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        BufferedWriter writer = Files.newWriter(new File(yearData), Charsets.UTF_8);
        for (int i = 0; i < yearList.size(); i++) {
            writer.write(i + "\t" + yearList.get(i) + "\r\n");
        }
        writer.close();
        return contents;
    }
}
