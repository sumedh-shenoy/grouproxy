package com.amazonaws.lambda.demo;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class textMessagingProxy implements RequestHandler<Object, String> {

    @Override
    public String handleRequest(Object input, Context context) {
    	
    	HashMap<String, String> mapInput = (HashMap<String, String>) input;
    	
        context.getLogger().log("Input: " + input);
        
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_WEST_1).build();
		DynamoDBMapper mapper = new DynamoDBMapper(client);
		
		String phoneNumber = mapInput.get("From");
		String message = mapInput.get("Body");
		
		try {
		    phoneNumber = java.net.URLDecoder.decode(phoneNumber, StandardCharsets.UTF_8.name());
		    message = java.net.URLDecoder.decode(message, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
		    return twimlGenerator.twiml("Sorry, an error occurred.");
		}
		
		RegistrationProcess response = new RegistrationProcess(mapper, phoneNumber, message);
		try {
			return response.execute();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return twimlGenerator.twiml("Sorry, you have entered an invalid command.");

    }

}
