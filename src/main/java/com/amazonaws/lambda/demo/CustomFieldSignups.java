package com.amazonaws.lambda.demo;

import java.util.Map;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

public class CustomFieldSignups {
	
	private DynamoDBMapper mapper;
	private String phoneNumber;
	private String message;
	private String username;
	
	public CustomFieldSignups(DynamoDBMapper Mapper, String PhoneNumber, String Message, String Username) {
		mapper = Mapper;
		phoneNumber = PhoneNumber;
		message = Message;
		username = Username;
	}
	
	public String signup(User user) {
		
		int step = user.getStep();
		
		Map<String, String> customProperties = user.getCustomProperties();
		customProperties.put(Initialize.customFieldNames[step], message);
		user.setStep(step + 1);
		user.setCustomProperties(customProperties);
		
		if(step + 1 >= Initialize.customFieldNames.length) {
			user.setStep(0);
			user.setState("none");
			
			mapper.save(user);
			
			return twimlGenerator.twiml("You've finished signing up—now you're ready to start using grouproxy! "
					+ "To start, message \"commands\" to " + user.getToPhonenumber() + ".");
		}
		
		mapper.save(user);
		return twimlGenerator.twiml(Initialize.customFieldQuestions[step + 1]);
	}

}
