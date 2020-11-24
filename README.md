NOTES AND COMMANDS FOR KAFKA:
------------------------------



Start Zookeeper in one command line: zookeeper-server-start.bat config\zookeeper.properties

Start Kafka in another command line: kafka-server-start.bat config\server.properties


Topics
--------

create topic: kafka-topics --zookeeper 127.0.0.1:2181 --topic first_topic --create --partitions 3 --replication-factor 1

kafka-topics --zookeeper 127.0.0.1:2181 --topic first_topic --describe

kafka-topics --zookeeper 127.0.0.1:2181 --topic first_topic --delete

Producer:
---------

kafka-console-producer --broker-list 127.0.0.1:9092 --topic first_topic

kafka-console-producer --broker-list 127.0.0.1:9092 --topic first_topic --producer-property acks=all

kafka-console-producer --broker-list 127.0.0.1:9092 --topic new_topic (Creates a topic which doesn't exist)


Consumer:
----------

kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic

kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic --from-beginning

kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic --group my-first-app (assign group id, can be assign to multiple consumer and the load is balanced)

kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic --group my-first-app --from-beginning(If this command runs twice the offset is written to kafka with group id and all the message won't be seen in the second run)


Consumer-group

kafka-consumer-groups

kafka-consumer-groups --bootstrap-server localhost:9092 --describe --group my-first-app

Reset offset : kafka-consumer-groups --bootstrap-server localhost:9092 --group  my-first-app --reset-offsets --to-earliest --execute --topic first_topic

Shift the offset pointer: kafka-consumer-groups --bootstrap-server localhost:9092 --group  my-first-app --reset-offsets --shift-by 2 --execute --topic first_topic(--shift-by -2 shifts the pointer backward)


Producer with key:
------------------
kafka-console-producer --broker-list 127.0.0.1:9092 --topic first_topic --property parse.key=true --property key.separator=,
> key,value
> another key,another value

Consumer with key:
-----------------

kafka-console-consumer --bootstrap-server 127.0.0.1:9092 --topic first_topic --from-beginning --property print.key=true --property key.separator=,



Conduktor - Kafka GUI
Kafka does not come bundled with a UI, but I have created a software name Conduktor to help you use Kafka visually.

You can download Conduktor here: https://www.conduktor.io/


Idempotent Producer:
--------------------
If the ack doesn't reach the producer when network error from Kafka. Idempotent producer setting identifies whether there is a duplicate msg or not. If so it will not be committed but the failed ack will be sent back

how to set it in java program : setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");

Safe Producer:
---------------
Kafka < 0.11
acks=all (producer level)
min.insync.replicas = 2 (broker level/topic level)
retries = MAX_INT (producer level)
max.in.flight.requests.per.connection=1 (producer level)

Kafka>=0.11
enable.idempotence=true(producer level) + min.insync.replicas=2(topic/ broker level)

Running a safe producer might impact throughput and latency

Linger.ms = number of milli second the producer waits before sending out a requests(by default=0) increasing the linger.ms=5 makes it possible to send messages in a batch by doing this it increases the throughput. Also the batch.size is considered while sending the message.

batch.size = Maximum number of bytes that will include in a batch(default=16KB)
Msg bigger than the batch size will not be batched

Max.block.ms & buffer.memory: If the producer produces message faster than the broker could consumer the incoming messages are stored into buffer space(default = 32MB)

if buffer is full then the send will be blocked and max.block.ms=60000ms that means the brokers are not able to consume messages and exception is thrown.


Delivery Semantics:
--------------------
atmost once: offsets are committed as soon as the message batch is received. If the processing goes wrong the message will be lost(it won't be read again)

|1|2|3|4|5| -> when committing this 5 messages if after committing 1,2,3 the consumer goes down. the msg 4 and 5 are lost.



At least once: offsets are committed after the messages is processed. If the processing goes wrong the message will be read again. This can result in duplicate processing of message. Make sure your processing is idempotent(i.e. processing again the message won't impact your system)


Exactly once: Can be achieved for Kafka=> Kafka communication using stream apis



Consumer Poll Behavior:
-----------------------
Kafka works in Poll model

Fetch.min.bytes(default: 1):
> controles how much data you want to pull at least on each request
>Helps increase through put decrease request number
>At the cost of latency

Max.poll.record(default 500)
>controls how many records to receive per poll request
>increase if you messages are very small and have a lot of memory

Max.partition.fetch.bytes(default 1MB)

fetch.max.bytes(default 50MB)


Consumer Offset Commit Strategy
-------------------------------
2 Strategy:
>enable.auto.commit = true & synchronous processing of batches
eg. while(true){
		List<Records> batch = consume.poll(Duration.ofMillis(100));
		doSomethingSynchronous(batch);
	}
	* With auto-commit offset will be committed automatically for you at regular interval(auto.commit.interval.ms=5000 by default)
	* If you don't use synchronous processing, you will be in "at-most-once" behaviour
	because offsets will be committed before your data is processed.
>enable.auto.commit = false & manual commit of offsets(synchronous processing of batches)
eg. while(true){
		List<Records> batch = consume.poll(Duration.ofMillis(100));
		if(isReady(batch)){
			doSomethingSynchronous(batch);
			consumer.commitSync();
		}
	}
	* control your commit offset and what's the condition for committing them
	* Example: accumulating records into a buffer and then flushing the buffer to a db + committing offsets then


Consumer Offset Reset Behaviour:
--------------------------------

* Consumer is expected to read for a log continuously
* But if your application has a bug, your consumer can be down for more than 7 days it will loose data

* The behaviour for the consumer is to then use:
>auto.offset.reset=latest : will read from the end of the log
>auto.offset.reset= earliest : will read from the beginning
>auto.offset.reset= none : will throw exception if no offset is found


Offset.retention.minutes can control the time the offset can be retained

Replay data:
> Take all the consumers from a specific group down
> Use Kafka-consumer-groups command to set offset to what you want
>restart consumers
