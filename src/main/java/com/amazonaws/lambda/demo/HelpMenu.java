package com.amazonaws.lambda.demo;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

public class HelpMenu {
	
	private DynamoDBMapper mapper;
	private String message;

	public HelpMenu(DynamoDBMapper Mapper, String Message) {
		mapper = Mapper;
		message = Message;
	}
	
	public String help() {
		if(!message.contains(":")) {
			return twimlGenerator.twiml("To get a tutorial on how to use a specific command name, enter \"commands: [command name]\"."
					+ " Valid command names are \"join\", \"message\", \"delete\", \"create\", \"invite\", \"leave\", and \"info\".");
		}
		
		String type = message.split(":")[1].trim().toLowerCase();
		
		if(type.equals("join")) {
			return twimlGenerator.twiml("Allows you to join an event. To use the join command, "
										+  " enter \"join: [desired event name]\".");
		}
		if(type.equals("message")) {
			return twimlGenerator.twiml("Allows you to message an event that you are a member of. To use the message command, "
					+ "\"enter message [event to be messaged]: [message you want to send]\".");
		}
		if(type.equals("delete")) {
			return twimlGenerator.twiml("Allows you to delete an event that you have created. To use the delete command, "
					+ "enter \"delete: [event to be deleted]\". Be warned, though: you cannot undo the deletion of a group!");
		}
		if(type.equals("create")) {
			return twimlGenerator.twiml("Allows you to create an event. Enter \"create\" to enter the group creation menu.");
		}
		if(type.equals("invite")) {
			return twimlGenerator.twiml("Allows you to invite users to an event. To invite a user to an event, enter "
					+ "\"invite [event to invite user to]; [username of user to invite]\"");
			
		}
		if(type.equals("leave")) {
			return twimlGenerator.twiml("Allows you to leave an event. To leave an event, enter"
					+ "\"leave: [event to leave]\". WARNING: if the event you are leaving requires an invite, "
					+ "you must be reinvited to join!");
		}
		if(type.equals("info")) {
			return twimlGenerator.twiml("Entering \"info\" will return a list of groups you are currently in, and entering "
					+ "info: [group] will return more information on [group].");
		}
		return twimlGenerator.twiml("Sorry, you have entered an invalid command to learn more about. Valid commands to "
				+ "learn more about are \"join\", \"message\", \"delete\", \"create\", \"invite\", \"leave\", and \"info\".");
	}

}
