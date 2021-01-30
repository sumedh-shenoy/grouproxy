package com.amazonaws.lambda.demo;

import java.io.IOException;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

public class MessageProcessor {
	
	private DynamoDBMapper mapper;
	private String phoneNumber;
	private String message;
	private String username;
	
	/*
	 * General commands:
	 * join: [group name] -> joins group if you have sufficient permissions
	 * message [group name]: message  -> pushes a message to a group if you have sufficient permissions
	 * message [group name] o: message  -> messages the admins of a group if you have sufficient permissions
	 * create group -> enters create group interface, given sufficient permissions
	 *      (a) first respond with group name, if it is taken, prompts for another one
	 *      (b) asks for date & time of event
	 *      (c) given sufficient permissions, asks whether to broadcast event to all members (yes/no)
	 *      (d) asks whether members can invite other members or not (yes/no)
	 *      (e) at any time, user can respond with "exit" to exit group creation interface, deleting the group
	 * delete: [group name] -> deletes group, if user has sufficient permissions
	 *      (a) asks for confirmation (yes/no)
	 *      (b) if delete is confirmed, sends a message to all those in the group about its deletion
	 * invite [group name]: [group name] -> invites member to group
	 * invite: admin [member name] [group name] -> invites member to group, and makes them an admin of the group
	 * leave: [group name] -> leaves group 
	 * help -> responds with help menu
	 * help: [command name] -> returns help menu for the command
	 */
	
	public MessageProcessor(DynamoDBMapper Mapper, String PhoneNumber, String Message, String Username) {
		mapper = Mapper;
		phoneNumber = PhoneNumber;
		message = Message;
		username = Username;
	}
	
	public String process() throws IOException {
		
		// first check if user is in any multistep actions
		User user = mapper.load(User.class, username);
		
		if(!user.getState().equals("none")) {
			
			if(user.getState().equals("signup")) {
				CustomFieldSignups custom = new CustomFieldSignups(mapper, phoneNumber, message, username);
				return custom.signup(user);
			} else {
				ManageEvent manage = new ManageEvent(mapper, message, username, user.getStep());
				return manage.manage(user);
			}
			
		}
		// now that we are done with multistep actiions, check message
		
		if(message.toLowerCase().startsWith("create") && (Initialize.memberEventCreation || user.getRole().equals("admin"))) {
			ManageEvent create = new ManageEvent(mapper, message, username, user.getStep());
			return create.create(user);
		}
		
		if(message.toLowerCase().startsWith("delete")) { 
			ManageEvent delete = new ManageEvent(mapper, message, username, user.getStep());
			return delete.delete(user);
		}
		
		if(message.toLowerCase().startsWith("message")) {
			SendMessage send = new SendMessage(mapper, username, message);
			return send.messageGroup(user);
		}
		
		if(message.toLowerCase().startsWith("commands")) {
			HelpMenu help = new HelpMenu(mapper, message);
			return help.help();
		}
		
		if(message.toLowerCase().startsWith("join")) {
			EventMembership join = new EventMembership(mapper, phoneNumber, message, username);
			return join.processJoin(user);
		}
		
		if(message.toLowerCase().startsWith("leave")) {
			EventMembership leave = new EventMembership(mapper, phoneNumber, message, username);
			return leave.processLeave();
		}
		
		if(message.toLowerCase().startsWith("invite")) {
			EventMembership invite = new EventMembership(mapper, phoneNumber, message, username);
			return invite.processInvite(user);
		}
		
		if(message.toLowerCase().startsWith("info")) {
			EventList eventList = new EventList(mapper, username);
			
			return eventList.process(message);
		}
		
		return twimlGenerator.twiml("Sorry, you have entered an invalid command. Enter \"help\" to see a detailed list of commands.");
	}

}
