package com.airisdata.utils;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.util.Utf8;

public class Meetup {

	String name;
	String meetup_date;
	Integer going;
	String organizer;
	String[] topics;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMeetup_date() {
		return meetup_date;
	}

	public void setMeetup_date(String meetup_date) {
		this.meetup_date = meetup_date;
	}

	public Integer getGoing() {
		return going;
	}

	public void setGoing(Integer going) {
		this.going = going;
	}

	public String getOrganizer() {
		return organizer;
	}

	public void setOrganizer(String organizer) {
		this.organizer = organizer;
	}

	public String[] getTopics() {
		return topics;
	}

	public void setTopics(String[] topics) {
		this.topics = topics;
	}

	public Meetup(String name, String date, Integer going,
			String organizer, String[] topics) {
		this.name = name;
		this.meetup_date = date;
		this.going = going;
		this.organizer = organizer;
		this.topics = topics;

	}

	/**
	 * This method serializes the java object into Avro record.
	 * 
	 * @return Avro generic record
	 */
	public GenericData.Record serialize(Schema schema) {
		GenericData.Record record = new GenericData.Record(schema);
		record.put("name", this.name);
		record.put("meetup_date", this.meetup_date);
		record.put("going", this.going);
		record.put("organizer", this.organizer);

		int nemails = (topics != null) ? this.topics.length : 0;

		GenericData.Array topics = new GenericData.Array(nemails, schema
				.getField("topics").schema());
		for (int i = 0; i < nemails; ++i)
			topics.add(new Utf8(this.topics[i]));

		record.put("topics", topics);

		return record;
	}

}
