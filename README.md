# avroparquet
AVRO / Parquet Demo Code

In a tool Directory

Parquet Command Line Tools

git clone -b apache-parquet-1.8.0 https://github.com/apache/parquet-mr.git

cd ./parquet-mr/parquet-tools/

mvn clean package -Plocal

cp parquet-tools/target/parquet-tools-1.8.0.jar .

java -jar ./parquet-tools-1.8.0.jar cat meetup_parquet.parquet

java -jar ./parquet-tools-1.8.0.jar schema meetup_parquet.parquet


AVRO Command Line Tools

You can download it from an Apache Mirror like:
http://apache.claz.org/avro/avro-1.8.0/java/avro-tools-1.8.0.jar

java -jar avro-tools-1.8.0.jar tojson --pretty $1

https://dzone.com/articles/getting-started-apache-avro
