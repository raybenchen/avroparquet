package com.airisdata.streamingutils

import java.io.ByteArrayOutputStream
import java.util

import events.avro.ClickEvent
import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificDatumWriter
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

import _root_.scala.util.Random

object EventGenerator extends App {
  
  if (args.length < 2) {
    System.err.println("Usage: KafkaWordCountProducer <metadataBrokerList> <topic>")
    System.exit(1)
  }

  val Array(brokers, topic) = args

  val props = new util.HashMap[String, Object]()
  props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers)

  // Kafka avro message stream comes in as a byte array
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.ByteArraySerializer")
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer")

  val producer = new KafkaProducer[String, Array[Byte]](props)

  // Generate events for every second and Send
  while(true) {
    
    val clickBytes = serializeClickEvent(newRandomClickEvent) 
    
    /**
     * 
     * Important: Producer Record that sends the message in Avro Serialized format
     * 
     */
    val message = new ProducerRecord[String, Array[Byte]](topic, null, clickBytes)
    
    producer.send(message)
    println("Event Prepared and Sent..!")

    Thread.sleep(1000)
  }

  def newRandomClickEvent: ClickEvent = {
    val userId = Random.nextInt(5) 
    val productId = Random.nextInt(5) 
    new ClickEvent(userId, productId)
  }

  def serializeClickEvent(clickEvent: ClickEvent): Array[Byte] = {
    val out = new ByteArrayOutputStream()
    val encoder = EncoderFactory.get.binaryEncoder(out, null)
    val writer = new SpecificDatumWriter[ClickEvent](ClickEvent.getClassSchema)

    writer.write(clickEvent, encoder)
    encoder.flush
    out.close
    out.toByteArray
  }
}