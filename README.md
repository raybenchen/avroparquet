# avroparquet
AVRO / Parquet Demo Code

In a tool Directory

git clone -b apache-parquet-1.8.0 https://github.com/apache/parquet-mr.git

cd ./parquet-mr/parquet-tools/

mvn clean package -Plocal

cp parquet-tools/target/parquet-tools-1.8.0.jar .

java -jar ./parquet-tools-1.8.0.jar cat meetup_parquet.parquet

java -jar ./parquet-tools-1.8.0.jar schema meetup_parquet.parquet

