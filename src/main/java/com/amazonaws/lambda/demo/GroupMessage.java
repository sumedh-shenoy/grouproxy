package com.amazonaws.lambda.demo;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedList;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.api.v2010.account.MessageCreator;
import com.twilio.rest.api.v2010.account.Call.Event;
import com.twilio.type.PhoneNumber;

public class GroupMessage {
	
	private DynamoDBMapper mapper;
	private String group;
	private String message;
	
	// textMessagingProxy version
	
    public static final String ACCOUNT_SID = "YOUR_SID_HERE";
    public static final String AUTH_TOKEN = "YOUR_AUTH_TOKEN_HERE";
	
	public GroupMessage(DynamoDBMapper mapper, String group, String message) {
		this.mapper = mapper;
		this.group = group;
		this.message = message;
	}
	
	public void sendGroupMessage() {
		
		Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
		
		// query the secondary index
		EventMember gsiQuery = new EventMember();
		gsiQuery.setEventID(group);
		
		DynamoDBQueryExpression<EventMember> queryExpression = 
				new DynamoDBQueryExpression<EventMember>()
				.withHashKeyValues(gsiQuery)
				.withIndexName("eventID-index")
				.withConsistentRead(false);
		
		PaginatedList<EventMember> members = mapper.query(EventMember.class, queryExpression);
		
		for(EventMember member : members) {
			User toMessage = mapper.load(User.class, member.getUsername());
			
			String toPhone = toMessage.getPhoneNumber();
			String fromPhone = toMessage.getToPhonenumber();
			
			System.out.println(toPhone + " " + fromPhone);
			
			Message m = Message.creator(new PhoneNumber(toPhone), new PhoneNumber(fromPhone), message).create();
		}
		
	}
	
	public void sendWelcomeMessage(String username) {
		
		Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
		User toMessage = mapper.load(User.class, username);
		
		String toPhone = toMessage.getPhoneNumber();
		String fromPhone = toMessage.getToPhonenumber();
		
		Message m = Message.creator(new PhoneNumber(toPhone), new PhoneNumber(fromPhone), message).create();
	}
	
	public void sendClosingMessage() {
		
		Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
		
		// query the secondary index
		EventMember gsiQuery = new EventMember();
		gsiQuery.setEventID(group);
		
		System.out.println(group);
		
		DynamoDBQueryExpression<EventMember> queryExpression = 
				new DynamoDBQueryExpression<EventMember>()
				.withHashKeyValues(gsiQuery)
				.withIndexName("eventID-index")
				.withConsistentRead(false);
		
		PaginatedList<EventMember> members = mapper.query(EventMember.class, queryExpression);
		
		ArrayList<EventMember> toDelete = new ArrayList<EventMember>();
		
		for(EventMember member : members) {
			User toMessage = mapper.load(User.class, member.getUsername());
			
			toDelete.add(member);
			
			if(toDelete.size() == 24) {
				mapper.batchDelete(toDelete);
				toDelete = new ArrayList<EventMember>();
			}
			
			String toPhone = toMessage.getPhoneNumber();
			String fromPhone = toMessage.getToPhonenumber();
			
			Message m = Message.creator(new PhoneNumber(toPhone), new PhoneNumber(fromPhone), message).create();
			
		}
		
		if(toDelete.size() > 0) {
			mapper.batchDelete(toDelete);
		}
		
	}
	
	public void sendAnnouncement() {
		
		Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
		
		PaginatedList<User> all = mapper.scan(User.class, new DynamoDBScanExpression());
		
		for(User user : all) {
			PhoneNumber toPhone = new PhoneNumber(user.getPhoneNumber());
			PhoneNumber fromPhone = new PhoneNumber(user.getToPhonenumber());
			
			Message m = Message.creator(toPhone, fromPhone, message).create();
		}
	}
}
