package com.amazonaws.lambda.demo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedList;

public class EventList {
	
	private DynamoDBMapper mapper;
	private String username;
	
	public EventList(DynamoDBMapper Mapper, String Username) {
		mapper = Mapper;
		username = Username;
	}
	
	public String process(String message) {
		
		if(message.contains(":")) {
			String groupName = message.split(":")[1].trim();
			
			return info(groupName);
		}
		
		return list();
	}
	
	public String list() {
		
		EventMember member = new EventMember();
		member.setUsername(username);
		
		DynamoDBQueryExpression<EventMember> queryExpression = 
				new DynamoDBQueryExpression<EventMember>()
				.withHashKeyValues(member);
		
		PaginatedList<EventMember> events = mapper.query(EventMember.class, queryExpression);
		
		String res = "You are in the event(s): ";
		
		for(EventMember event : events) {
			res = res + event.getEventID() + ", ";
		}
		if(events.size() > 0) {
			res = res.substring(0, res.length() - 2); // removes the last ", "
		} else {
			res = "You are not currently in any events.";
		}
		
		return twimlGenerator.twiml(res);
	}
	
	public String info(String group) {
		
		EventMember membership = new EventMember(username, group);
		EventMember check = mapper.load(membership);
		
		if(check == null) {
			return twimlGenerator.twiml("Sorry, you are not in this event.");
		}
		
		Event event = mapper.load(Event.class, group);
		if(event == null) {
			return twimlGenerator.twiml("Sorry, this event does not exist.");
		}
		
		LocalDateTime eventTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTime()), TimeZone.getDefault().toZoneId());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
		String eventTimeFormat = formatter.format(eventTime);
		
		String toReturn = "You are a(n) " + check.getRole() + " of " + group + ", which is occuring"
				+ " on " + eventTimeFormat + "."; 
		
		
		return twimlGenerator.twiml(toReturn);
	}

}
