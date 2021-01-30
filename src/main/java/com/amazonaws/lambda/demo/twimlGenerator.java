package com.amazonaws.lambda.demo;

public class twimlGenerator {
	
	public static String twiml(String body) {
		String toReturn = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <Response>\n" + 
				"    <Message><Body>" + body + "</Body></Message>\n" + 
						"</Response>";
		
		return toReturn;
	}

}
