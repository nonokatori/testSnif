package spark;

import dbworker.DBWorker;
import kafka.ProducerKafka;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import sniffer.PacketSniff;

import java.nio.file.Paths;

public class SparkStream {

    private final DBWorker dbWorker;
    private final ProducerKafka kafka;

    //для работы под ОС Windows
    static {
        String OS = System.getProperty("os.name").toLowerCase();

        if (OS.contains("win")) {
            System.setProperty("hadoop.home.dir", Paths.get("src/main/resources/").toAbsolutePath().toString());
        } else {
            System.setProperty("hadoop.home.dir", "/");
        }
    }

    public SparkStream(DBWorker dbWorker, ProducerKafka kafka) {
        this.dbWorker = dbWorker;
        this.kafka = kafka;
    }


    //запуск потока спарка
    public void start() {
        JavaStreamingContext jsc = new JavaStreamingContext("local[2]", "TrafficLimits", Durations.seconds(20));
        JavaReceiverInputDStream<String> messages = jsc.socketTextStream("localhost", 6379);
        JavaDStream<PacketSniff> packages = messages.map(PacketSniff::new);
        int limitTime1 = 300;
        int limitTime2 = 60;
        kafka.startKafkaProducer();
        //подсчет суммарного объема трафика в течении 5 минут
        packages.map(PacketSniff::getSize)
                .reduceByWindow(Long::sum, Durations.seconds(limitTime1), Durations.seconds(limitTime2))
                .foreachRDD(rdd ->
                        rdd.collect().forEach(size -> {
                            if (size > dbWorker.getMax()) {
                                kafka.send(size, "Max limit. Traffic amount = ");
                            }
                            if (size < dbWorker.getMin()) {
                                kafka.send(size, "Min limit. Traffic amount = ");
                            }
                        })
                );
        jsc.start();

        try {
            jsc.awaitTermination();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        kafka.getProducer().close();
    }
}