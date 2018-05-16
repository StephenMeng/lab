package com.stephen.lab.controller.message;

import com.stephen.lab.service.message.KafkaService;
import com.stephen.lab.util.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("kafka")
public class KafkaController {
    @Autowired
    private KafkaService kafkaService;
    @RequestMapping("test")
    public Response test(){
        String topic="testTopic";
        new Thread(() -> {
            for(int i = 0; i<10; i++){
                kafkaService.produce(topic,"message:"+i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        kafkaService.consume(topic);
        return Response.success("");
    }
}
