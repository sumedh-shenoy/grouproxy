package com.amazonaws.lambda.demo;

import java.util.HashMap;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class eventAlertHandler implements RequestHandler<Object, String> {

	// textMessagingProxy version
	
    @Override
    public String handleRequest(Object input, Context context) {
        context.getLogger().log("Input: " + input);
        
        HashMap<String, String> map = (HashMap<String, String>) input;

        EventAlert.duration = Long.parseLong(map.get("duration"));
        
        EventAlert alert = new EventAlert();
        alert.pushAlert();
        
        return "Alerts sent.";
    }

}
