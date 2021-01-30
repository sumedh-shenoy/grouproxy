package com.amazonaws.lambda.demo;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

public class SendMessage {
	private DynamoDBMapper mapper;
	private String username;
	private String message;
	
	public SendMessage(DynamoDBMapper Mapper, String Username, String Message) {
		mapper = Mapper;
		username = Username;
		message = Message;
	}
	
	public String messageOrganizer() {
		
		// add to message queue
		
		// TODO: finiish this later
		
		return "";
	}
	
	public String messageGroup(User user) {
		
		// add to message queue
		
		if(!message.contains(":")) {
			return twimlGenerator.twiml("You have entered an invalid command. " + 
										"For details on how to use the join command, please text \"help join\".");
		}
		
		String keys [] = message.split(":")[0].trim().split(" ");
		String messageContent = message.split(":")[1].trim();
		
		if(keys.length < 2 || messageContent.equals("")) {
			return twimlGenerator.twiml("You have entered an invalid command. " + 
					"For details on how to use the join command, please text \"help join\".");
		}
		
		String groupID = keys[1];
		messageContent = username + "_" + groupID + ": " + messageContent;
		
		EventMember messager = mapper.load(EventMember.class, username, groupID);
		
		if(messager == null && !user.getRole().equals("admin")) {
			return twimlGenerator.twiml("Sorry, you are not part of this group.");
		}
		
		Event eventToMessage = mapper.load(Event.class, groupID);
		Integer size = eventToMessage.getNumMessages();
		size = size == null ? 1 : size + 1;
		eventToMessage.setNumMessages(size);
		
		Message toSend = new Message(groupID, size, messageContent);
		mapper.batchSave(eventToMessage, toSend);
	//	mapper.delete(toSend);
		
		return twimlGenerator.twiml("Message sent, it may take a moment to be delivered.");
	}
	
	public void welcomeMessage(User user, Event event) {
		Integer size = event.getNumMessages();
		size = size == null ? 1 : size + 1;
		
		String messageContent = "You have been added to " + event.getID() + "! \n Description: "  + event.getDescription();
		
		Message toSend = new Message(event.getID(), size, messageContent);
		toSend.setType("welcome");
		toSend.setRecipient(user.getUsername());
		event.setNumMessages(size);
		
		mapper.batchSave(event, toSend);
		
	//	mapper.delete(toSend);
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
