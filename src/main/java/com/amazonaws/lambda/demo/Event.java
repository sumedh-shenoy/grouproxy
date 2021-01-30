package com.amazonaws.lambda.demo;

import java.util.Set;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName="EventList")
public class Event {
	
	private String id;
	private Set<String> admins;
	private Boolean allowJoins;
	private Boolean allowRequests;
	private Boolean allowMessages;
	private Boolean broadcast;
	private Integer numMessages;
	private Integer size;
	private Integer index;
	private Long time;
	private Long deleteTime;
	private String description;
	
	public Event() {
		index = 0;
		size = 0;
	}
	
	@DynamoDBHashKey(attributeName="id") 
	public String getID() { return id; }
	public void setID(String id) { this.id = id; }
	
	@DynamoDBAttribute(attributeName="admins")
	public Set<String> getAdmins() { return admins; }
	public void setAdmins(Set<String> admins) { this.admins = admins; }
	
	@DynamoDBAttribute(attributeName="allowBroadcast")
	public Boolean getBroadcast() { return broadcast; }
	public void setBroadcast(Boolean broadcast) { this.broadcast = broadcast; }
	
	@DynamoDBAttribute(attributeName="allowJoins")
	public Boolean getAllowJoins() { return allowJoins; }
	public void setAllowJoins(Boolean allowJoins) { this.allowJoins = allowJoins; }
	
	@DynamoDBAttribute(attributeName="allowRequests")
	public Boolean getAllowRequests() { return allowRequests; }
	public void setAllowRequests(Boolean allowRequests) { this.allowRequests = allowRequests; }
	
	@DynamoDBAttribute(attributeName="allowMessages")
	public Boolean getAllowMessages() { return allowMessages; }
	public void setAllowMessages(Boolean allowMessages) { this.allowMessages = allowMessages; }
	
	@DynamoDBAttribute(attributeName="numMessages")
	public Integer getNumMessages() {
		return numMessages;
	}
	public void setNumMessages(Integer numMessages) {
		this.numMessages = numMessages;
	}
	
	@DynamoDBIndexRangeKey(attributeName="eventTime", globalSecondaryIndexName="constantIndex-index") 
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}
	
	@DynamoDBAttribute(attributeName="size")
	public Integer getSize() {
		return size;
	}
	public void setSize(Integer size) {
		this.size = size;
	}

	@DynamoDBIndexHashKey(attributeName="constantIndex", globalSecondaryIndexName="constantIndex-index")
	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	@DynamoDBAttribute(attributeName="description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@DynamoDBAttribute(attributeName="deleteTime")
	public Long getDeleteTime() {
		return deleteTime;
	}

	public void setDeleteTime(Long deleteTime) {
		this.deleteTime = deleteTime;
	}

	
	
	
}
