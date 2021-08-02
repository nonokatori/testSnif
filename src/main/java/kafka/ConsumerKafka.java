
package kafka;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import json.JSONWorker;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

public class ConsumerKafka {
    private Consumer<Long, String> consumer;
    private final String port;
    private final String ip;

    public ConsumerKafka(JSONWorker jw) {
        this.port = jw.getPort_kafka();
        this.ip = jw.getIp_kafka();
    }

    public void startKafkaConsumer() {
        TopicPartition tp = new TopicPartition("update_db", 0);
        List<TopicPartition> tps = Arrays.asList(tp);
        Properties props = new Properties();
        props.put("bootstrap.servers", this.ip + ":" + this.port);
        props.put("group.id", "app_group");
        props.put("key.deserializer", LongDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        this.consumer = new KafkaConsumer(props);
        this.consumer.assign(tps);
        this.consumer.seekToBeginning(tps);
    }

    public boolean hasUpdate() {
        ConsumerRecords<Long, String> consumerRecords = this.consumer.poll(Duration.ofSeconds(5L));
        return consumerRecords.isEmpty();
    }
}
