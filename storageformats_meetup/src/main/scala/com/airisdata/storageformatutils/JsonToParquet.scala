package com.airisdata.storageformatutils

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.avro.generic.GenericRecord
import org.apache.spark.sql.functions._
import org.apache.hadoop.hdfs.server.namenode.INodeReference.WithName
import com.databricks.spark.avro._;
import org.apache.spark.sql.hive.HiveContext

object JsonToParquet {
  def main(args: Array[String]) {
    val conf = new SparkConf;
    
    /**
     * configuration to set compression codec for parquet
     */
    
    conf.set("spark.sql.parquet.compression.codec","snappy");
    
    val sc = new SparkContext(conf);
    val sqlContext = new HiveContext(sc);

    val zipCodesDF = sqlContext.read.json(args(0))
    
    /**
     * Column renames
     */
    val newDataFrame = zipCodesDF.withColumnRenamed("_id", "locationId").withColumnRenamed("loc", "lat_long")
    
    import sqlContext.implicits._;
    val df2 = newDataFrame.withColumn("lat", newDataFrame("city")+",USA")
    
    /**
     * Writing the data to Parquet file.
     */
    //newDataFrame.write.parquet(args(1))
    
    //df2.write.mode("append").parquet(args(1));
    
    /**
     * Creating hive external table
     */
    
    //sqlContext.createExternalTable("parquet_data", args(1), "parquet");
    

  }
}