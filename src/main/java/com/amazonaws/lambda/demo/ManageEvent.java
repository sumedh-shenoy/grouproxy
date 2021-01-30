package com.amazonaws.lambda.demo;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;

public class ManageEvent {
	
	private DynamoDBMapper mapper;
	private String message;
	private String username;
	private Integer step;
	
	public ManageEvent(DynamoDBMapper Mapper, String Message, String Username, Integer Step) {
		mapper = Mapper;
		message = Message;
		username = Username;
		step = Step;
	} 
	
	public String manage(User user) {
		int step = user.getStep();
		if(step < 0) {
			return delete(user);
		} 
		
		return create(user);
	}
	
	public String create(User user) {
		
		String value = message;
		
		if(value.toLowerCase().trim().startsWith("exit")) {
			
			String tableName = user.getState();
			user.setStep(0);
			user.setState("none");
			
			Event clear = mapper.load(Event.class, tableName);
			
			if(clear != null) {
				mapper.delete(clear);
			}
			mapper.save(user);
			
			return twimlGenerator.twiml("Event creation menu exited.");
		}
		
		if(step == 0) {
			user.setStep(1);
			user.setState("create");
			
			mapper.save(user);
			return twimlGenerator.twiml("What would you like your event to be named? " + 
										"Non-alphanumeric characters are not allowed (other than '_'), and event names must be unique.");
		}
		
		if(step == 1) {
			// creating an event named value
			Event isEvent = mapper.load(Event.class, value);
			if(isEvent != null) {
				return twimlGenerator.twiml("Sorry, this name is already taken. Please provide a different name for your event.");
			}
			if(!alphanumeric(value)) {
				return twimlGenerator.twiml("Sorry, your event's name contains special characters. Please provide a different name"
						+ " for your event.");
			}
			
			Event toAdd = new Event();
			toAdd.setID(value);
			
			Set<String> admins = new HashSet<>();
			admins.add(username);
			toAdd.setAdmins(admins);
			
			user.setStep(2);
			user.setState(value);
			
			mapper.batchSave(toAdd, user);
			
			return twimlGenerator.twiml("Please enter a brief description of your event.");
			
		}
		if(step == 2) {
			// creating event description
			Event event = mapper.load(Event.class, user.getState());
			event.setDescription(value);
			user.setStep(3);
			
			mapper.batchSave(event, user);
			
			return twimlGenerator.twiml("When is your event ocurring (in Central Time)? Please respond in the format HH:MM MM/DD/YYYY. "+ 
					"For example, an event occuring at 1:15 PM on August 1st, 2021 would be represented as 13:15 08/01/2021.");
		}
		if(step == 3) {
			// creating an event with time & date of value
			
			String [] components = value.split(" ");
			
			if(components.length != 2) {
				return twimlGenerator.twiml("Sorry, that is an invalid time and date.");
			}
			
			String times [] = components[0].split(":");
			String dates [] = components[1].split("/");
			
			if(times.length != 2 || dates.length != 3) {
				return twimlGenerator.twiml("Sorry, that is an invalid time and date.");
			}
			
			long time = 0;
			
			try {
				
				int year = Integer.parseInt(dates[2]);
				int month = Integer.parseInt(dates[0]);
				int day = Integer.parseInt(dates[1]);
				
				int hour = Integer.parseInt(times[0]);
				int minute = Integer.parseInt(times[1]);
				
				LocalDateTime eventTime = LocalDateTime.of(year, month, day, hour, minute);
				Date date = Date.from(eventTime.atZone(ZoneId.systemDefault()).toInstant());
				time = date.getTime();
				
				Date now = Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
				if(time <= now.getTime()) {
					return twimlGenerator.twiml("Sorry, the event time and date must be after the current time and date.");
				}
				
			} 
			catch(Exception e) {
				return twimlGenerator.twiml("Sorry, that is an invalid time and date.");
			}
			Event event = mapper.load(Event.class, user.getState());
			
			event.setTime(time);
			user.setStep(4);
			
			mapper.batchSave(user, event);
			
			return twimlGenerator.twiml("What time would you like messaging for your event to close? Please respond in the format HH:MM MM/DD/YYYY." 
									+	"For example, an event occuring at 1:15 PM on August 1st, 2021 would be represented as 13:15 08/01/2021.");
		}
		if(step == 4) {
			// creating an event with delete time
			String [] components = value.split(" ");
			
			if(components.length != 2) {
				return twimlGenerator.twiml("Sorry, that is an invalid time and date.");
			}
			
			String times [] = components[0].split(":");
			String dates [] = components[1].split("/");
			
			if(times.length != 2 || dates.length != 3) {
				return twimlGenerator.twiml("Sorry, that is an invalid time and date.");
			}
			
			long time = 0;
			
			try {
				
				int year = Integer.parseInt(dates[2]);
				int month = Integer.parseInt(dates[0]);
				int day = Integer.parseInt(dates[1]);
				
				int hour = Integer.parseInt(times[0]);
				int minute = Integer.parseInt(times[1]);
				
				LocalDateTime eventTime = LocalDateTime.of(year, month, day, hour, minute);
				Date date = Date.from(eventTime.atZone(ZoneId.systemDefault()).toInstant());
				time = date.getTime();
				
			} 
			catch(Exception e) {
				return twimlGenerator.twiml("Sorry, that is an invalid time and date.");
			}
			Event event = mapper.load(Event.class, user.getState());
			
			if(time < event.getTime()) {
				return twimlGenerator.twiml("Sorry, the closing date must be after the event date.");
			}
			
			event.setDeleteTime(time);
			user.setStep(5);
			
			mapper.batchSave(user, event);
			
			return twimlGenerator.twiml("Would you like your event to be invite only? Please enter yes or no.");
		}
		
		if(step == 5) {
			boolean allowJoins = false;
			if(value.equalsIgnoreCase("no")) {
				allowJoins = true;
			} else if(value.equalsIgnoreCase("yes")) {
				allowJoins = false;
			} else {
				return twimlGenerator.twiml("You have entered an invalid response. Please enter yes or no.");
			}
			
			Event event = mapper.load(Event.class, user.getState());
			event.setAllowJoins(allowJoins);
			user.setStep(6);
			
			mapper.batchSave(event, user);
			
			return twimlGenerator.twiml("Would you like members to be able to message all event members? Please enter yes or no.");
		}
		if(step == 6) {
			boolean allowMessages = false;
			if(value.equalsIgnoreCase("yes")) {
				allowMessages = true;
			} else if(value.equalsIgnoreCase("no")) {
				allowMessages = false;
			} else {
				return twimlGenerator.twiml("You have entered an invalid response. Please enter yes or no.");
			}
			
			Event event = mapper.load(Event.class, user.getState());
			EventMember auto = new EventMember(username, user.getState(), "admin");
			event.setAllowMessages(allowMessages);
			user.setStep(7);

			if(Initialize.memberEventGlobalNotification) {
				mapper.batchSave(event, user);
				
				return twimlGenerator.twiml("Would you like to broadcast the creation of this event to all registered users? Please enter yes or no.");
			}
			
			event.setBroadcast(false);
			user.setState("none");
			user.setStep(0);
			
			mapper.batchSave(event, user, auto);
			return twimlGenerator.twiml("Succesfully created the event " + event.getID() + ".");
		}
		if(step == 7) {
			boolean broadcast = false;
			if(value.equalsIgnoreCase("yes")) {
				broadcast = true;
			} else if(value.equalsIgnoreCase("no")) {
				broadcast = false;
			} else {
				return twimlGenerator.twiml("You have entered an invalid response. Please enter yes or no.");
			}
			
			Event event = mapper.load(Event.class, user.getState());
			EventMember auto = new EventMember(username, user.getState(), "admin");
			event.setAllowMessages(broadcast);
			user.setStep(0);
			user.setState("none");
			
			mapper.batchSave(event, user, auto);
			
			if(Initialize.memberEventGlobalNotification && broadcast) {
				String groupID = "all";
				
				Event eventToMessage = mapper.load(Event.class, groupID);
				Integer size = eventToMessage.getNumMessages();
				size = size == null ? 1 : size + 1;
				eventToMessage.setNumMessages(size);
				
				Message toSend = new Message(groupID, size, "New Event: " + event.getID() + "\n Description: " + event.getDescription());
				toSend.setType("all");
				mapper.batchSave(eventToMessage, toSend);
			}
			
			return twimlGenerator.twiml("Succesfully created the event " + event.getID());
		}
		
		return "";
	}
	
	public String delete(User user) {
		
		if(user.getStep() == 0) {
			if(!message.contains(":")) {
				return twimlGenerator.twiml("You have entered an invalid command. " + 
											"For details on how to use the delete command, please text \"command: delete\".");
			}
			
			String value = message.split(":")[1].trim();
			
			Event toRemove = mapper.load(Event.class, value);
			
			if(toRemove == null) {
				return twimlGenerator.twiml("Sorry, that event does not exist.");
			}
			
			if(!toRemove.getAdmins().contains(username) && !user.getRole().equals("admin")) {
				return twimlGenerator.twiml("Sorry, you do not have the permissions to delete that event.");
			}
			
			user.setStep(-1);
			user.setState(value);
			mapper.save(user);
			
			return twimlGenerator.twiml("Reply with \"confirm\" if you are sure that you want to delete " + value +".");
		}
		
		String groupName = user.getState();
		Event toRemove = mapper.load(Event.class, groupName);
		
		SendMessage deleteGroup = new SendMessage(mapper, username, "");
		deleteGroup.closingMessage(toRemove);
		
		user.setState("none");
		user.setStep(0);
		
		// TODO: query event table for all, and delete
		// TODO: compile messages into a single document, perhaps add an additional lambda function
		/*
		EventMember gsiQuery = new EventMember();
		gsiQuery.setEventID(groupName);
		
		DynamoDBQueryExpression<EventMember> queryExpression = 
				new DynamoDBQueryExpression<EventMember>()
				.withHashKeyValues(gsiQuery)
				.withIndexName("eventID-index")
				.withConsistentRead(false);
		
		List<EventMember> members = mapper.query(EventMember.class, queryExpression);
		ArrayList<EventMember> toRemove = new ArrayList<>();
		
		for(EventMember member : members) {
			if(toRemove.size() == 24) {
				mapper.batchDelete(toRemove);
				toRemove = new ArrayList<>();
			}
			
			toRemove.add(member);
		}
		
		if(toRemove.size() != 0) {
			mapper.batchDelete(toRemove);
		}
		*/
		
		mapper.save(user);
		return twimlGenerator.twiml("Deleting " + groupName+".");

	}
	
	private boolean alphanumeric(String s) {
		for(char c : s.toCharArray()) {
			if(!Character.isLetterOrDigit(c) && c != '_')  {
				return false;
			}
		}
		return true;
	}

}
