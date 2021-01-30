package com.amazonaws.lambda.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

public class Configure {
	
	// textMessagingProxy version

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		
		AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion(Regions.US_WEST_1).build();
		DynamoDBMapper mapper = new DynamoDBMapper(client);
		
		Scanner in = new Scanner(new File("phonenumbers"));
		
		boolean valid = true;
		String initializeClass = "package com.amazonaws.lambda.demo;  \n\n";
		
		initializeClass+=("public class Initialize { \n");
		
		ArrayList<String> nums = new ArrayList<String>();
		
		while(in.hasNext()) {
			nums.add(in.nextLine());
		}
		
		initializeClass+=("static int numberOfPhones = " + nums.size() + "; \n");
		
		initializeClass+=("static String phones [] = {");
		
		for(int i = 0; i < nums.size(); i++) {
			if(i + 1 == nums.size()) {
				initializeClass+=("\""+nums.get(i) + "\"}; \n");
			} else {
				initializeClass+=("\""+nums.get(i) + "\", ");
			}
		}
		
		in = new Scanner(new File("config"));
		
		for(int i = 0; i < 3; i++) {
			String nextLine = in.nextLine();
			if(nextLine.startsWith("alertTime")) {
				initializeClass+=("static int " + nextLine + ";\n");
			} else {
				initializeClass+=("static boolean " + nextLine + ";\n");
			}
		}
		
		in = new Scanner(new File("customFields"));
		
		ArrayList<String> keys = new ArrayList<String>();
		ArrayList<String> questions = new ArrayList<String>();
		
		while(in.hasNext()) {
			String next = in.nextLine();
			keys.add(next);
			if(!in.hasNext()) {
				System.out.println("Invalid customFields file!");
				valid = false;
				continue;
			}
			next = in.nextLine();
			questions.add(next);
		}
		
		initializeClass+=("static String [] customFieldNames = {");
		for(int i = 0; i < keys.size(); i++) {
			if(i + 1 == keys.size()) {
				initializeClass+=("\"" + keys.get(i) + "\"};\n");
			} else {
				initializeClass+=("\"" + keys.get(i) + "\", ");
			}
		}
		
		initializeClass+=("static String [] customFieldQuestions = {");
		for(int i = 0; i < questions.size(); i++) {
			if(i + 1 == questions.size()) {
				initializeClass+=("\"" + questions.get(i) + "\"};\n");
			} else {
				initializeClass+=("\"" + questions.get(i) + "\",");
			}
		}
		
		in = new Scanner(new File("admins"));
		ArrayList<String> admins = new ArrayList<String>();
		
		while(in.hasNext()) {
			admins.add(in.nextLine());
		}
		
		if(admins.size() == 0) {
			System.out.println("Sorry, you must specify at least one admin.");
			valid = false;
		}
		
		initializeClass+=("static String [] adminNumbers = {");
		for(int i = 0; i < admins.size(); i++) {
			if(i + 1 == admins.size()) {
				initializeClass += ("\"" + admins.get(i) + "\"};\n");
			} else {
				initializeClass += ("\"" + admins.get(i) + "\", ");
			}
		}
		
		initializeClass+="}";
		System.out.println("Please replace the contents of Initialize.java with the following: \n");
		if(valid) {
			System.out.println(initializeClass);
		}
		
		Event all = mapper.load(Event.class, "all");
		
		if(all == null) {
			// add an event called all
			Event toAdd = new Event();
			toAdd.setID("all");
			toAdd.setAllowJoins(true);
			toAdd.setAllowMessages(false);
			toAdd.setAllowRequests(true);
			toAdd.setBroadcast(false);
			toAdd.setTime(0L);
			toAdd.setDeleteTime(0L);
			toAdd.setDescription("Group containing all users, where announcements are broadcasted.");
			
			mapper.save(toAdd);
		}
		
	}

}
