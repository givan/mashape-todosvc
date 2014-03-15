package com.mashape.todosvc.clients;

import com.mashape.todosvc.model.Todo;
import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.SmsFactory;
import com.twilio.sdk.resource.instance.Sms;

import java.util.HashMap;
import java.util.Map;

public class TodoCompletedSms {
    public boolean send(Todo completedTodo, String fromMobileNumber) throws TwilioRestException {
        // TODO: put these in a config file
        TwilioRestClient client =
                new TwilioRestClient("AC38e5dcc213c1f452831196989048369e", "67a9323cdfdda49f25cf8e2234d5838f");

        // Build a filter for the SmsList
        Map<String, String> params = new HashMap<String, String>();
        params.put("Body", completedTodo.getTitle() + " - COMPLETED");
        params.put("To", "+" + fromMobileNumber);
        params.put("From", "+19207791418");  // this is my trial Twilio number;


        // TODO: we can do this async - will improve perf
        SmsFactory messageFactory = client.getAccount().getSmsFactory();
        Sms message = messageFactory.create(params);
        return message.getSid() != null && message.getSid().length() > 0;
    }
}
