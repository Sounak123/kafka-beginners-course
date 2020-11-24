package kafka.beginners;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemoWithCallback {
    public static void main(String[] args) {
        final Logger logger = LoggerFactory.getLogger(ProducerDemoWithCallback.class);

        final String bootstrapServer = "127.0.0.1:9092";
        //create producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapServer);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());


        //create a producer
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);

        //create a producer record
        ProducerRecord<String, String> record = new ProducerRecord<String, String>("first_topic", "hello World with callback");

        //send data
        producer.send(record, new Callback() {
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                //executes every time a record is successfully send or exception is thrown
                if(e==null){
                    //the record metadata
                    logger.info("Received new metadata. \n"+
                            "Topic:"+recordMetadata.topic()+"\n"+
                                    "Partition:"+recordMetadata.partition()+"\n"+
                                    "Offset:"+recordMetadata.offset()+"\n"+
                                    "Timestamp:"+recordMetadata.timestamp()+"\n"
                            );
                }else{
                    logger.error("Error while producing", e);
                }
            }
        });

        //flush data
        producer.flush();

        //flush and close producer
        producer.close();

    }
}
