package kafka;

import com.stephen.lab.Application;
import com.stephen.lab.service.message.KafkaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class KafkaServiceTest {
    @Autowired
    private KafkaService kafkaService;
    @Test
    public void testKafka(){
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
        new Thread(() -> {
            kafkaService.consume(topic);
        }).start();
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        kafkaService.wakeup();
    }
}
