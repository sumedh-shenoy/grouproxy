package com.amazonaws.lambda.demo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

public class EventAlert {
	
	// textMessagingProxy version
	
	static long duration = 24L;
	static long durationM = duration;
	private DynamoDBMapper mapper;
	
	public EventAlert() {
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_WEST_1).build();
		mapper = new DynamoDBMapper(client);
		// load duration in here
		durationM = duration*60L*1000L;
	}
	
	public void pushAlert() {
		
		LocalDateTime now = LocalDateTime.now();
		
		Long current = Date.from(now.atZone(ZoneId.systemDefault()).toInstant()).getTime();
		Long next = current + 2*durationM;
		
		// System.out.println(current + durationM/2);
		
        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":val1", new AttributeValue().withN("0"));
        eav.put(":val2", new AttributeValue().withN(Long.toString(next)));

		
		DynamoDBQueryExpression<Event> queryExpression = 
				new DynamoDBQueryExpression<Event>()
				.withIndexName("constantIndex-index")
				.withKeyConditionExpression("constantIndex = :val1 and eventTime < :val2")
				.withExpressionAttributeValues(eav)
				.withConsistentRead(false);
		
		List<Event> toAlert = mapper.query(Event.class, queryExpression);
		
		for(Event event : toAlert) {
			if(event == null || event.getID().equals("all")) continue;
			
			System.out.println(event.getID());
			
			if(event.getDeleteTime() < current) {
				// delete it, we're done here
				DeleteEvent delete = new DeleteEvent(mapper);
				delete.closingMessage(event);
				
				continue;
			}
			
			Integer size = event.getNumMessages();
			size = size == null ? 1 : size + 1;
			event.setNumMessages(size);
			
			LocalDateTime eventTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(event.getTime()), ZoneId.of("America/Chicago"));
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
			String eventTimeFormat = formatter.format(eventTime);
			
			Message toSend = new Message(event.getID(), size, "The event " + event.getID() + " occurs within " + 
										(duration/60) + " hours, at " + eventTimeFormat + ".");
			
			mapper.batchSave(event, toSend);
			
		}
		
	}

}
