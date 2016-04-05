-- Schema Evolution with Hive Example
-- Create an External table with Avro Schema -- This can be tried with external or managed table 
-- Use the meetup_avro.avro file for the data. 

CREATE external TABLE avro_external_table
  ROW FORMAT SERDE
  'org.apache.hadoop.hive.serde2.avro.AvroSerDe'
  STORED AS INPUTFORMAT
  'org.apache.hadoop.hive.ql.io.avro.AvroContainerInputFormat'
  OUTPUTFORMAT
  'org.apache.hadoop.hive.ql.io.avro.AvroContainerOutputFormat'
  LOCATION 'hdfs://quickstart.cloudera:8020/user/cloudera/avro_data/'
  TBLPROPERTIES (
    'avro.schema.url'='hdfs://localhost:8020/user/cloudera/schema.avsc');

-- Select query to check the data
Select * from avro_external_table

-- Alter table statement to alter the schema file, to check the schema evolution
ALTER TABLE avro_external_table SET TBLPROPERTIES ('avro.schema.url'='hdfs://localhost:8020/user/cloudera/schema3.avsc');

-- Select query
Select * from avro_external_table

------------------
-- Parquet Table

-- Compression code. This can be set to SNAPPY, GZIP or UNCOMPRESSED
set parquet.compression=SNAPPY;

create table parquet_table (going INT, name STRING)
  ROW FORMAT SERDE 'parquet.hive.serde.ParquetHiveSerDe'
  STORED AS 
    INPUTFORMAT "parquet.hive.DeprecatedParquetInputFormat"
    OUTPUTFORMAT "parquet.hive.DeprecatedParquetOutputFormat"
    LOCATION '/user/cloudera/parquet_meetup/';

INSERT OVERWRITE TABLE parquet_table SELECT going,name FROM avro_external_table2;

-- Impala tables are much easier
create table parquet_table_name (going INT, name STRING) STORED AS PARQUET;

