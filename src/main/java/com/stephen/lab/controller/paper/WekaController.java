package com.stephen.lab.controller.paper;

import com.stephen.lab.model.paper.KivaSimple;
import com.stephen.lab.service.paper.KivaService;
import com.stephen.lab.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by stephen on 2018/3/13.
 */
@RequestMapping("weka")
public class WekaController {
    @Autowired
    private KivaService kivaService;

    @RequestMapping("test")
    public Response getTFIDFResults() {
        List<KivaResult> kivaResults = new ArrayList<>();
        List<KivaSimple> kivaList = kivaService.selectAllSimple();
        return Response.success("");
    }

    private Instances generatePopularInstance(List<KivaSimple> entities) {
        List<String> classies = Arrays.asList("1", "0");
        //set attributes
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.add(new Attribute("text"));
        attributes.add(new Attribute("isTag", classies));
        //set instances
        Instances instances = new Instances("test", attributes, 0);
        instances.setClassIndex(instances.numAttributes() - 1);
        //add instance
        for (KivaSimple kivaSimple : entities) {
            Instance instance = new DenseInstance(attributes.size());
            instance.setValue(0, kivaSimple.getStandardDescription());
            instance.setValue(1, kivaSimple.getId() > 10 ? "1" : "0");
            instances.add(instance);
        }
        return instances;
    }
}
