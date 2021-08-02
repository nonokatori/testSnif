package kafka;

import java.util.Properties;
import json.JSONWorker;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

public class ProducerKafka {
    private Producer<String, String> producer;
    private final String port;
    private final String ip;

    public ProducerKafka(JSONWorker jsonWorker) {
        this.port = jsonWorker.getPort_kafka();
        this.ip = jsonWorker.getIp_kafka();
    }

    public void startKafkaProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", this.ip + ":" + this.port);
        props.put("key.serializer", StringSerializer.class.getName());
        props.put("value.serializer", StringSerializer.class.getName());
        props.put("transactional.id", "trans_id");
        this.producer = new KafkaProducer(props);
        this.producer.initTransactions();
    }

    public void send(long size, String s) {
        this.producer.beginTransaction();
        this.producer.send(new ProducerRecord("alerts", s + size + " bytes. "));
        this.producer.commitTransaction();
    }

    public Producer<String, String> getProducer() {
        return this.producer;
    }
}
