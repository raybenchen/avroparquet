package com.airisdata.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.avro.AvroSchemaConverter;
import org.apache.parquet.avro.AvroWriteSupport;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.schema.MessageType;

public class StorageFormatUtils {

	/**
	 * Writes Java objects into a binary Avro-encoded file
	 */
	public static void writeAvroFile(File file, Meetup[] Meetups,
			Schema schema) throws IOException {
		GenericDatumWriter datum = new GenericDatumWriter(schema);
		DataFileWriter writer = new DataFileWriter(datum);

		writer.create(schema, file);
		for (Meetup p : Meetups)
			writer.append(p.serialize(schema));

		writer.close();
	}

	/**
	 * Writes out Java objects into a JSON-encoded file
	 */
	public static void writeAvroJsonEncoding(File file, Meetup[] Meetups,Schema schema) throws IOException {
		GenericDatumWriter writer = new GenericDatumWriter(schema);
		Encoder e = EncoderFactory.get().jsonEncoder(schema, new FileOutputStream(file));

		for (Meetup p : Meetups)
			writer.write(p.serialize(schema), e);

		e.flush();
	}

	/**
	 * Reads in binary Avro-encoded entities using the schema stored in the file
	 * and prints them out.
	 */
	public static void readAvroFile(File file) throws IOException {
		GenericDatumReader datum = new GenericDatumReader();
		DataFileReader reader = new DataFileReader(file, datum);

		GenericData.Record record = new GenericData.Record(reader.getSchema());
		while (reader.hasNext()) {
			reader.next(record);
			System.out.println("Name " + record.get("name") + " on "
					+ record.get("Meetup_date") + " attending "
					+ record.get("going") + " organized by  "
					+ record.get("organizer") + " on  " + record.get("topics"));
		}

		reader.close();
	}

	/**
	 * Reads in binary Avro-encoded entities using a schema that is different
	 * from the writer's schema.
	 * 
	 */
	public static void readWithDifferentSchema(File file, Schema newSchema)
			throws IOException {
		GenericDatumReader datum = new GenericDatumReader(newSchema);
		DataFileReader reader = new DataFileReader(file, datum);

		GenericData.Record record = new GenericData.Record(newSchema);
		while (reader.hasNext()) {
			reader.next(record);
			System.out.println("Name " + record.get("name") + " on "
					+ record.get("Meetup_date") + " attending "
					+ record.get("attendance") + " organized by  "
					+ record.get("organizer") 
					+ " at  " + record.get("location"));
		}

		reader.close();
	}
	
	/**
	 * Writes Java objects into a binary Avro-encoded file
	 */
	public static void writeParquetFile(File file, Meetup[] Meetups,
			Schema schema) throws IOException {
		
		MessageType parquetSchema = new AvroSchemaConverter().convert(schema);
		
		// create a WriteSupport object to serialize your Avro objects
		AvroWriteSupport writeSupport = new AvroWriteSupport(parquetSchema, schema);

		CompressionCodecName compressionCodecName = CompressionCodecName.SNAPPY;
		int blockSize = 256 * 1024 * 1024;
		int pageSize = 64 * 1024;
		
		Path outputPath = new Path(file.getAbsolutePath());
		
		ParquetWriter parquetWriter = new ParquetWriter(outputPath,writeSupport, compressionCodecName, blockSize, pageSize);
		
		for (Meetup p : Meetups)
			parquetWriter.write(p.serialize(schema));

		parquetWriter.close();
	}
	
	/**
	 * Reads in binary Avro-encoded entities using a schema that is different
	 * from the writer's schema.
	 * 
	 */
	public static void readParquet(File file) throws IOException {
		
		System.out.println("Reading parquet file");
		AvroParquetReader reader = new AvroParquetReader(new Path(file.getAbsolutePath()));

		GenericData.Record record = (Record) reader.read();
		while (record != null) {

			System.out.println("Name " + record.get("name") + " on "
					+ record.get("Meetup_date") + " attending "
					+ record.get("attendance") + " organized by  "
					+ record.get("organizer") 
					+ " at  " + record.get("location"));
			
			record = (Record) reader.read();
		}

		reader.close();
	}
	
	/**
	 * Main Method
	 */

	public static void main(String[] args) {
		Meetup Meetup1 = new Meetup("Spark-H20", "2016-01-01",
				50, "airisdata", new String[] { "h2o","repeated shopper prediction" });
		Meetup Meetup2 = new Meetup("Spark-Avro", "2016-01-02",
				60, "airisdata", new String[] { "avro", "usecases" });
		Meetup Meetup3 = new Meetup("Spark-Parquet",
				"2016-01-03", 70, "airisdata", new String[] { "parquet","usecases" });
		Meetup[] Meetups = new Meetup[] { Meetup1, Meetup2,
				Meetup3 };
		
		String input = args[0];
		String option = args[1];
		File outputFile = new File(args[2]);
		
		Schema oldSchema = null;
		if(args.length > 3){
			try {
				oldSchema = new Schema.Parser().parse(FileUtils.readFileToString(new File(args[3])));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {

			/**
			 * Writes Avro file
			 */
			if(option.equalsIgnoreCase("write") && input.equalsIgnoreCase("avro")){
				System.out.println("writing avro file with schema " + args[3]);
				writeAvroFile(outputFile, Meetups, oldSchema);
				System.out.println("finished...!!!");
			}
			
			
			/**
			 * Writes parquet file
			 */
			if(option.equalsIgnoreCase("write") && input.equalsIgnoreCase("parquet")){
				System.out.println("writing parquet file with schema " + args[3]);
				writeParquetFile(outputFile, Meetups, oldSchema);
				System.out.println("finished...!!!");
			}
			

			/**
			 * Writes Avro file
			 */
		
			if(option.equalsIgnoreCase("read") && input.equalsIgnoreCase("avro")){
				if(oldSchema != null){
					System.out.println("*********Reading Avro file with a schema *********");
					readWithDifferentSchema(outputFile, oldSchema);
					System.out.println("finished...!!!");
				}else{
					/**
					 * Read avro file without schema
					 */
					System.out.println("*********Reading Avro file without schema, Schema will be derifed from avro file *********");
					readAvroFile(outputFile);
					System.out.println("finished...!!!");
				}
			}else if(option.equalsIgnoreCase("read") && input.equalsIgnoreCase("parquet")){
				System.out.println("*********Reading Parquet file with a schema *********");
				readParquet(outputFile);
				System.out.println("finished...!!!");
			}
			
			//System.out.println("*********Writing Avro file with Json Encoding *********");

			//writeAvroJsonEncoding(jsonfile,Meetups,oldSchema);
			
			//System.out.println("*********----**********----*********");
			
		} catch (IOException e) {
			System.out.println("Main: " + e.getMessage());
		}
	}

}

