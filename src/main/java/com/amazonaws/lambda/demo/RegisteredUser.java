package com.amazonaws.lambda.demo;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName="SignedupUsers")
public class RegisteredUser {
	
	/*
	 * phoneNumber: implicit
	 * username: implicit
	 * step: what step of signup user is on
	 *    0 = currently setting username
	 *    1 = finished signing up
	 */
	
	private String phoneNumber;
	private String name;
	private Integer step;
	
	public RegisteredUser(String phoneNumber, String username) {
		this.phoneNumber = phoneNumber;
		this.step = 0;
		
		char mix [] = username.toCharArray();
		for(int i = 0; i < mix.length; i++) {
			mix[i]-=1;
		}
		this.name = new String(mix);
	}
	
	public RegisteredUser(String phoneNumber) {
		this.phoneNumber = phoneNumber;
		this.step = 0;
	}
	
	public RegisteredUser() {
		this.step = 0;
	}
	
	@DynamoDBHashKey(attributeName="phoneNumber")
	public String getPhoneNumber() { 
		return this.phoneNumber; 
	}
	public void setPhoneNumber(String phoneNumber) { 
		this.phoneNumber = phoneNumber; 
	}
	
	@DynamoDBAttribute(attributeName="username")
	public String getName() {
		return name;
	}
	public void setName(String name) { 		
		this.name = name;
	}
	
	@DynamoDBAttribute(attributeName="step")
	public Integer getStep() { return this.step; }
	public void setStep(Integer step) { this.step = step; }
	
	
	@DynamoDBIgnore
	public String getUsername() {
		String s = getName();
		if(s == null) return null;
		
		char c [] = s.toCharArray();
		for(int i = 0; i < c.length; i++) {
			c[i] = (char) ((c[i] + 67)%127);
		}
		
		return new String(c);
	}
	
	@DynamoDBIgnore
	public void setUsername(String username) {
		char c [] = username.toCharArray();
		for(int i = 0; i < c.length; i++) {
			c[i] = (char) ((c[i] + 60)%127);
		}
		setName(new String(c));
	}
	
}
