package com.airisdata.streamingutils

import events.avro.ClickEvent
import kafka.serializer.DefaultDecoder
import org.apache.avro.io.DecoderFactory
import org.apache.avro.specific.SpecificDatumReader
import org.apache.spark._
import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Minutes, Seconds, StreamingContext}

object KafkaAvroConsumer extends App {
  
  if (args.length < 2) {
    System.err.println("Usage: <topic> <numThreads>")
    System.exit(1)
  }

  val Array(topics, numThreads) = args

  val sparkConf = new SparkConf().setAppName("AvroMeetupExample").setMaster("local[2]")
  val ssc = new StreamingContext(sparkConf, Seconds(2))
  
  /**
   * Check Point directory
   */
  ssc.checkpoint("./checkpointDir")

  val kafkaConf = Map(
    "metadata.broker.list" -> "localhost:9092", 
    "zookeeper.connect" -> "localhost:2181", 
    "group.id" -> "kafka-streaming-example",
    "zookeeper.connection.timeout.ms" -> "1000")

  val topicMap = topics.split(",").map((_, numThreads.toInt)).toMap

  val lines = KafkaUtils.createStream[String, Array[Byte], DefaultDecoder, DefaultDecoder](ssc, kafkaConf, topicMap, StorageLevel.MEMORY_ONLY_SER).map(_._2)

  val userNameMapRDD = ssc.sparkContext.parallelize(Array((1,"User1"), (2, "User2"), (3, "User3"), (4, "User4"), (5, "User5")))
  val meetupNameMapRDD = ssc.sparkContext.parallelize(Array((1,"Spark-Avro"), (2, "Spark-Parquet"), (3, "Spark-H2O"), (4, "Spark-Streaming"), (5, "Spark-ML")))

  val mappedUserName = lines.transform{rdd =>
    val eventRDD: RDD[(Int, Int)] = rdd.map {
      
      /**
       * Decode the Avro event to normal data
       * 
       */
      
       bytes => AvroUtil.clickEventDecode(bytes)    
    }.map{ 
         streamEvent => (streamEvent.getUserId: Int) -> streamEvent.getProductId
    }
    eventRDD.join(userNameMapRDD).map { case (userId, (productId, userName)) => (userName, productId)}
  }

  val mappedMeetupId = mappedUserName.transform{ rdd =>
    val productRDD = rdd.map { case (userName, productId) => (productId: Int, userName) }

    productRDD.join(meetupNameMapRDD).map { case (productId, (productName, userName)) => (userName, productName)}
  }

  val clickCounts = mappedMeetupId.map(x => (x, 1L))
    .reduceByKeyAndWindow(_ + _, _ - _, Minutes(2), Seconds(2), 2).map { case ((productName, userName), count) =>
    (userName, productName, count)
  }

  clickCounts.print 

  ssc.start()
  ssc.awaitTermination()
}

object AvroUtil {
  val reader = new SpecificDatumReader[ClickEvent](ClickEvent.getClassSchema)
  def clickEventDecode(bytes: Array[Byte]): ClickEvent = {
    val decoder = DecoderFactory.get.binaryDecoder(bytes, null)
    reader.read(null, decoder)
  }
}