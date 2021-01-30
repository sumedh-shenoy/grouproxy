package com.amazonaws.lambda.demo;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIgnore;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName="UserList")
public class User {

	private String username;
	private String phone;
	private Map<String, String> customProperties;
	private String state = "none";
	private Integer step;
	private String toPhonenumber;
	private String role; // member, admin

	public User() {
		this.role = "member";
		this.step = 0;
		customProperties = new HashMap<>();
	}
	
	
	public User(String username, String phoneNumber, String toPhone) {
		this.username = username;
		this.toPhonenumber = toPhone;
		this.role = "member";
		this.step = 0;
		customProperties = new HashMap<>();
		setPhoneNumber(phoneNumber);
	}
	
	public User(String username, String phoneNumber, String toPhone, String role) {
		this.username = username;
		this.toPhonenumber = toPhone;
		this.role = role;
		this.step = 0;
		customProperties = new HashMap<>();
		setPhoneNumber(phoneNumber);
	}
	
	
	@DynamoDBHashKey(attributeName="username")
	public String getUsername() { 
		return username; 
	}
	public void setUsername(String username) { 
		this.username = username; 
	}
	
	
	@DynamoDBAttribute(attributeName="phoneNumber")
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	
	@DynamoDBAttribute(attributeName="customProperties")
	public Map<String, String> getCustomProperties() { 
		return customProperties; 
	}
	public void setCustomProperties(Map<String, String> customProperties) {
		this.customProperties = customProperties;
	}
	
	
	@DynamoDBAttribute(attributeName="state")
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	
	
	@DynamoDBAttribute(attributeName="step")
	public Integer getStep() {
		return step;
	}
	public void setStep(Integer step) {
		this.step = step;
	}
	
	
	@DynamoDBAttribute(attributeName="role")
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	
	
	@DynamoDBAttribute(attributeName="toPhone")
	public String getToPhonenumber() {
		return toPhonenumber;
	}
	public void setToPhonenumber(String toPhonenumber) {
		this.toPhonenumber = toPhonenumber;
	}

	@DynamoDBIgnore
	public String getPhoneNumber() {
		if(phone == null) return null;
		
		char c [] = phone.toCharArray();
		for(int i = 0; i < c.length; i++) {
			c[i] = (char) ((c[i] + 67)%127);
		}
		
		return new String(c);
	}
	
	public void setPhoneNumber(String phoneNumber) {
		char c [] = phoneNumber.toCharArray();
		for(int i = 0; i < c.length; i++) {
			c[i] = (char) ((c[i] + 60)%127);
		}
		
		this.phone = new String(c);
	}
	
}
