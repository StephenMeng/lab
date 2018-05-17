package com.stephen.lab.service.message;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface KafkaService {
    void produce(String topic,String message);

    ConsumerRecord consume(String topic);

    void wakeup();
}
