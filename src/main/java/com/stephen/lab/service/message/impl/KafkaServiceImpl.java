package com.stephen.lab.service.message.impl;

import com.google.common.collect.Lists;
import com.stephen.lab.service.message.KafkaService;
import com.stephen.lab.util.LogRecod;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class KafkaServiceImpl implements KafkaService {
    private static Properties kafaProducerProps =new Properties();
    private static Properties kafaConsumerProps =new Properties();

    private static Producer producer;
    private static Consumer consumer;
    static {
        kafaProducerProps.put("bootstrap.servers","localhost:9092");
        kafaProducerProps.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        kafaProducerProps.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
        producer=new KafkaProducer(kafaProducerProps);

        kafaConsumerProps.put("bootstrap.servers","localhost:9092");
        kafaConsumerProps.put("group.id","consumer group");
        kafaConsumerProps.put("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        kafaConsumerProps.put("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        consumer=new KafkaConsumer(kafaConsumerProps);
    }

    @Override
    public void produce(String topic ,String message) {
        ProducerRecord<String,String> record=new ProducerRecord<>(topic,"key",message);
        try {
            producer.send(record);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public ConsumerRecord consume(String topic) {
         consumer.subscribe(Lists.newArrayList(topic));
         try {
         while (true){
            ConsumerRecords<String,String> records=consumer.poll(100);
            for(ConsumerRecord record:records){
                LogRecod.print(record.key());
                LogRecod.print(record.value());
            }
         }}catch (Exception e) {
             e.printStackTrace();
         }finally {consumer.close();
         }
         return null;
    }

    @Override
    public void wakeup() {
        consumer.wakeup();
    }
}
