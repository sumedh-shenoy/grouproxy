package com.amazonaws.lambda.demo;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

public class EventMembership {
	
	private DynamoDBMapper mapper;
	private String phoneNumber;
	private String message;
	private String username;
	
	public EventMembership(DynamoDBMapper Mapper, String PhoneNumber, String Message, String Username) {
		mapper = Mapper;
		phoneNumber = PhoneNumber;
		message = Message;
		username = Username;
	}
	
	public String processJoin(User user) {
		
		if(!message.contains(":")) {
			return twimlGenerator.twiml("You have entered an invalid command. " + 
										"For details on how to use the join command, please text \"commands: join\".");
		}
		
		String key = message.split(":")[1].trim();
		
		Event toJoin = mapper.load(Event.class, key);
		
		if(toJoin == null) {
			return twimlGenerator.twiml("Sorry, this event does not exist.");
		}
		
		boolean allowJoins = toJoin.getAllowJoins();
		EventMember newMember = new EventMember(username, key);
		
		if(mapper.load(newMember) != null) {
			return twimlGenerator.twiml("You're already part of this event.");
		}
		
		if(allowJoins || user.getRole().equals("admin")) {
			newMember.setRole("member");
			mapper.save(newMember);
			
			SendMessage welcome = new SendMessage(mapper, username, "");
			welcome.welcomeMessage(user, toJoin);
			
			return twimlGenerator.twiml("You have succesfully joined " + key+".");
			
		} else {
			return twimlGenerator.twiml("Sorry, to join this event you must be invited by an admin of the event.");
		}
		
	}
	
	public String processLeave() {
		
		if(!message.contains(":")) {
			return twimlGenerator.twiml("You have entered an invalid command. " + 
										"For details on how to use the leave command, please text \"commands: leave\".");
		}
		
		String key = message.split(":")[1].trim();
		EventMember toRemove = mapper.load(EventMember.class, username, key);
		
		if(toRemove == null) {
			// TODO: generate twiml for saying that event does not exist, or you are not part of it
			return twimlGenerator.twiml("Sorry, either that event does not exist, or you are not a part of it.");
		}
		
		mapper.delete(toRemove);
	
		// TODO: generate twiml for saying you have left the event, and will stop recieving notifications
		return twimlGenerator.twiml("You have left " + key + ", and will stop recieving messages from it.");
	}
	
	public String processInvite(User adder) {
		
		if(!message.contains(":")) {
			return twimlGenerator.twiml("You have entered an invalid command. " + 
										"For details on how to use the invite command, please text \"help invite\".");
		}
		
		String keys [] = message.split(":")[0].trim().split(" ");
		String values [] = message.split(":")[1].trim().split(" ");
		
		if(keys.length < 2 || values.length > 1) {
			System.out.println(keys.length + " " + values.length);
			return twimlGenerator.twiml("You have entered an invalid command. " + 
										"For details on how to use the invite command, please text \"help join\".");
		}
		
		String username = values[0];
		String eventID = keys[1];
		
		User user = mapper.load(User.class, username);
		
		if(user == null) {
			// TODO: generate twiml for saying that user does not exist
			
			return twimlGenerator.twiml("Sorry, the user you tried to invite does not exist.");
		}
		
		Event event = mapper.load(Event.class, eventID);
		
		if(adder.getRole().equals("member") && (!event.getAllowJoins() && !event.getAdmins().contains(this.username))) {
			return twimlGenerator.twiml("Sorry, the group you have tried to invite " + username + " to only allows invites from admins.");
		}
		
		// otherewise, add user
		
		EventMember newMember = new EventMember(username, eventID, "member");
		mapper.save(newMember);

		SendMessage welcome = new SendMessage(mapper, username, "");
		
		welcome.welcomeMessage(user, event);
		return twimlGenerator.twiml(username + " has been succesfully added to " + eventID+"!");
	}
	
	public String processMultipleInvite() {
		return twimlGenerator.twiml("WIP.");
	}
	

}
