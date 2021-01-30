package com.amazonaws.lambda.demo;

import java.util.Map;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;

public class PushMessage implements RequestHandler<DynamodbEvent, Integer> {
	
	// textMessagingProxy project version
	
	static AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_WEST_1).build();
	static DynamoDBMapper mapper = new DynamoDBMapper(client);

    @Override
    public Integer handleRequest(DynamodbEvent event, Context context) {
        context.getLogger().log("Received event: " + event);

        for (DynamodbStreamRecord record : event.getRecords()) {
            
            Map<String, AttributeValue> item = record.getDynamodb().getNewImage();
            
            if(item == null || record.getEventName().equals("DELETE")) {
            	continue;
            } else {
            	
	            String group = item.get("groupID").getS();
	            String message = item.get("message").getS();
	            String type = item.get("type").getS();
	            String recipient = item.get("recipient").getS();
	            
	            GroupMessage g = new GroupMessage(mapper, group, message);
	            
	            if(type.equals("message")) {
	            	g.sendGroupMessage();
	            }
	            if(type.equals("welcome")) {
	            	g.sendWelcomeMessage(recipient);
	            }
	            if(type.equals("delete")) {
	            	g.sendClosingMessage();
	            }
	            if(type.equals("all")) {
	            	g.sendAnnouncement();
	            }
            }
        }
        return event.getRecords().size();
    }
}