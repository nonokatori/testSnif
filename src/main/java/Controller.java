
import dbworker.DBWorker;
import java.io.IOException;
import java.sql.SQLException;
import json.JSONWorker;
import kafka.ConsumerKafka;
import kafka.ProducerKafka;
import org.pcap4j.core.PcapNativeException;
import sniffer.Sniffer;
import spark.SparkStream;

public class Controller {
    public Controller() {
    }

    public static void main(String[] args) {
        Sniffer sniffer = new Sniffer();
        JSONWorker jsonWorker = new JSONWorker();
        DBWorker dbWorker = new DBWorker(jsonWorker);
        ProducerKafka kafka = new ProducerKafka(jsonWorker);
        SparkStream sparkStream = new SparkStream(dbWorker, kafka);
        ConsumerKafka kafkaConsumer = new ConsumerKafka(jsonWorker);
        dbWorker.startDB();
        int updateTime = 1200000;
        Thread threadSniff = new Thread(() -> {
            while(true) {
                try {
                    sniffer.start();
                } catch (PcapNativeException var2) {
                    var2.printStackTrace();
                } catch (IOException var3) {
                    var3.printStackTrace();
                }
            }
        });
        threadSniff.start();
        Thread threadDB = new Thread(() -> {
            while(!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(1200000L);
                    dbWorker.updateLimit();
                } catch (SQLException | ClassNotFoundException | InterruptedException var2) {
                    var2.printStackTrace();
                }
            }

        });
        threadDB.start();
        Thread threadSpark = new Thread(() -> {
            try {
                Thread.sleep(30000L);
                sparkStream.start();
            } catch (InterruptedException var2) {
                var2.printStackTrace();
            }

        });
        threadSpark.start();
        Thread threadConKafka = new Thread(() -> {
            kafkaConsumer.startKafkaConsumer();

            while(!Thread.currentThread().isInterrupted()) {
                if (kafkaConsumer.hasUpdate()) {
                    try {
                        dbWorker.updateLimit();
                    } catch (SQLException var3) {
                        var3.printStackTrace();
                    } catch (ClassNotFoundException var4) {
                        var4.printStackTrace();
                    }
                }
            }

        });
        threadConKafka.start();
    }
}
