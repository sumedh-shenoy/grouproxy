package com.amazonaws.lambda.demo;

import java.io.IOException;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

public class RegistrationProcess {
	
	/*
	 * TODO: sending reminder message, parsing dates, deleting members from groups when group is deleted
	 */
	
	private DynamoDBMapper mapper;
	private String phoneNumber;
	private String message;
	
	public RegistrationProcess(DynamoDBMapper Mapper, String PhoneNumber, String Message) {
		mapper = Mapper;
		phoneNumber = PhoneNumber;
		message = Message;
	}
	
	public boolean isRegistered() {

		RegisteredUser retrievedUser = mapper.load(RegisteredUser.class, phoneNumber);
		
		if(retrievedUser == null || retrievedUser.getStep() < 1) {
			return false;
		}
		
		return true;
	}
	
	public boolean isAvailable(String username) {
		
		User retrievedUser = mapper.load(User.class, username);
		
		if(retrievedUser == null) {
			return true;
		}
		return false;
	}
	
	public String execute() throws IOException {
		
		RegisteredUser retrievedUser = mapper.load(RegisteredUser.class, phoneNumber);
		
		if(retrievedUser != null && retrievedUser.getStep() < 1) {
			// TODO: complete the next method, which is processMessage()
			MessageProcessor next = new MessageProcessor(mapper, phoneNumber, message, retrievedUser.getUsername());
			
			return next.process();
		}
		
		if(retrievedUser == null) {
			RegisteredUser newUser = new RegisteredUser(phoneNumber);
			newUser.setStep(2);
		//	newUser.setPhoneNumber(phoneNumber);
			
			mapper.save(newUser);
			
			return twimlGenerator.twiml("Welcome to grouproxy! Please reply with the username that you would like to use."); 
		}	
		
		if(retrievedUser.getStep() == 2) {
			if(!isAvailable(message)) {
				return twimlGenerator.twiml("Sorry, this username is already taken. Please choose another one.");
				
			}
			
			retrievedUser.setUsername(message);
			retrievedUser.setStep(1);
			mapper.save(retrievedUser);
	
			return twimlGenerator.twiml("Do you want " + message + " to be your username? If so, type \"confirm\".");
		}
		if(message.toLowerCase().startsWith("confirm")) {
			retrievedUser.setStep(0);
			
			Event all = mapper.load(Event.class, "all");
			
			int pos = all.getSize()%Initialize.numberOfPhones;
			all.setSize(all.getSize() + 1);
			User newUser = new User(retrievedUser.getUsername(), retrievedUser.getPhoneNumber(), Initialize.phones[pos]);
			
			boolean isAdmin = false;
			
			for(String number : Initialize.adminNumbers) {
				if(number.equals(retrievedUser.getPhoneNumber())) {
					isAdmin = true;
				}
			}
			
			if(isAdmin) {
				newUser.setRole("admin");
			}
			
			if(Initialize.customFieldNames.length > 0) {
				newUser.setState("signup");
				mapper.save(newUser);
				
				mapper.batchSave(retrievedUser, newUser, all);
				// TODO: return first question
				return twimlGenerator.twiml(Initialize.customFieldQuestions[0]);
			}
			
			mapper.batchSave(retrievedUser, newUser, all);
			return twimlGenerator.twiml("Welcome, " + newUser.getUsername() + "!");
		} 
		
		retrievedUser.setStep(2);
		mapper.save(retrievedUser);
		return twimlGenerator.twiml("Please reply with the username that you would like to use.");
		
	}

}
