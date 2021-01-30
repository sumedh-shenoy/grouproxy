package com.amazonaws.lambda.demo;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName="MessageQueue")
public class Message {
	
	private String groupID;
	private Integer number;
	private String message;
	private String type;
	private String recipient;
	
	@DynamoDBHashKey(attributeName="groupID") 
	public String getGroupID() {
		return groupID;
	}
	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}
	
	@DynamoDBRangeKey(attributeName="number") 
	public Integer getNumber() {
		return number;
	}
	public void setNumber(Integer number) {
		this.number = number;
	}
	
	@DynamoDBAttribute(attributeName="message") 
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	@DynamoDBAttribute(attributeName="type")
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	@DynamoDBAttribute(attributeName="recipient")
	public String getRecipient() {
		return recipient;
	}
	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	
	
	public Message(String GroupID, Integer Number, String Message) {
		groupID = GroupID;
		number = Number;
		message = Message;
		type = "message";
		recipient = "group";
	}
	
	public Message() {
		
	}

}
