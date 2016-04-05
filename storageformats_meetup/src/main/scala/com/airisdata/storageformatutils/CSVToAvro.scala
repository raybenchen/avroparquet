package com.airisdata.storageformatutils

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.avro.generic.GenericRecord
import org.apache.spark.sql.functions._
import org.apache.hadoop.hdfs.server.namenode.INodeReference.WithName
import com.databricks.spark.avro._
import org.apache.avro.mapred.AvroKey
import org.apache.avro.mapreduce.AvroKeyOutputFormat
import org.apache.hadoop.io.NullWritable
import org.apache.avro.mapreduce.AvroJob
import org.apache.hadoop.mapreduce.Job
import org.apache.avro.Schema
import org.apache.avro.mapreduce.AvroKeyInputFormat
import org.apache.avro.reflect.ReflectData
import org.apache.avro.generic.GenericData
import java.io.File
import org.apache.avro.generic.GenericData.Record
import org.apache.avro.SchemaBuilder

case class Input(val input:String);

object CSVToAvro {
  def main(args: Array[String]) {
    val conf = new SparkConf;
    //conf.setMaster("local");
    conf.setAppName("CSVToAvro");
    val sc = new SparkContext(conf);
    val sqlContext = new SQLContext(sc);

    /**
     * Writing a String RDD to Avro
     * 
     */
    val rdd = sc.textFile(args(0))
     val job = Job.getInstance
     val schema = SchemaBuilder.record("Meetup")
      .fields.name("meetup_name").`type`().stringType().noDefault()
      .name("meetup_date").`type`().stringType().noDefault().name("going").`type`().intType().noDefault()
      .name("organizer").`type`().stringType().noDefault().endRecord
      
     AvroJob.setOutputKeySchema(job, schema)

     val linesRDD = rdd.map(line => {
       val innerSchema = SchemaBuilder.record("Meetup")
      .fields.name("meetup_name").`type`().stringType().noDefault()
      .name("meetup_date").`type`().stringType().noDefault().name("going").`type`().intType().noDefault()
      .name("organizer").`type`().stringType().noDefault().endRecord
      
        val record = new GenericData.Record(innerSchema)
       val data = line.split(",")
       record.put("meetup_name", data(0))
       record.put("meetup_date", data(1))
       record.put("going", data(2).toInt)
       record.put("organizer", data(3))
       
       (new AvroKey(record),NullWritable.get);
     })
     
     
     
     linesRDD.saveAsNewAPIHadoopFile(args(1),classOf[AvroKey[GenericRecord]],classOf[NullWritable],
                            classOf[AvroKeyOutputFormat[GenericRecord]],
                            job.getConfiguration)
                            
                            
    /**
     * Reading avro file as RDD
     * 
     */
                            
     val avroRdd = sc.newAPIHadoopFile(args(1), 
              classOf[AvroKeyInputFormat[GenericRecord]], 
              classOf[AvroKey[GenericRecord]], 
              classOf[NullWritable]);
     
     println("**********");
     
    avroRdd.foreach{case(x,y) => 
        {
          val record = x.datum()
          println(
              "Meetup name is " + record.get("meetup_name") + " --- " +  
              "Meetup Date is " + record.get("meetup_name") + " --- " + 
              "Attendence is " + record.get("going") + " --- " +
              "organizer is " + record.get("organizer") + " --- " 
              )
        }
      
      }
    
    println("**********");                           
    
    /**
     * Writing avro file from DataFrame
     */
    val inputDF = sqlContext.read.format("com.databricks.spark.csv").option("header", "false")
      .option("delimiter", ",")
      .option("inferSchema", "true")
      .load(args(0))

    import com.databricks.spark.avro._;
    inputDF.write.avro(args(2));
    /**
     * Reading avro file to DataFrame
     */
    
    
    val df = sqlContext.read.avro(args(2))
    
    println("*************");
      df.show();  
    println("*************");
    
    
  }
}