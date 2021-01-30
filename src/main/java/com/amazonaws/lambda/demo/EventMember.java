package com.amazonaws.lambda.demo;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName="EventMemberList")
public class EventMember {
	
	private String username;
	private String eventID;
	private String role; // member, moderator, admin
	
	public EventMember() {
		
	}
	
	public EventMember(String username, String eventID) {
		this.username = username;
		this.eventID = eventID;
	}
	
	public EventMember(String username, String eventID, String role) {
		this.username = username;
		this.eventID = eventID;
		this.role = role;
	}
	
	@DynamoDBHashKey(attributeName="username")
	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }
	
	@DynamoDBRangeKey(attributeName="eventID")
	@DynamoDBIndexHashKey(attributeName="eventID", globalSecondaryIndexName="eventID-index")
	public String getEventID() { return eventID; }
	public void setEventID(String eventID) { 
		this.eventID = eventID; 
	}
	
	@DynamoDBAttribute(attributeName="role")
	public String getRole() { return role; }
	public void setRole(String role) { this.role = role; }
	


	
}
