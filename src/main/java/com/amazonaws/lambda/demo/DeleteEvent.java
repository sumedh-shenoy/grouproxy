package com.amazonaws.lambda.demo;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

public class DeleteEvent {
	
	// textMessagingProxy version
	
	private DynamoDBMapper mapper;
	
	public DeleteEvent(DynamoDBMapper Mapper) {
		mapper = Mapper;
	}
	
	public void closingMessage(Event event) {
		Integer size = event.getNumMessages();
		size = size == null ? 1 : size + 1;
		
		String messageContent = "The event group " + event.getID() + " has closed, and"
				+ " will no longer send and recieve messages.";
		
		Message toSend = new Message(event.getID(), size, messageContent);
		toSend.setType("delete");
		
		mapper.save(toSend);
		mapper.batchDelete(event);
	}
	

}
