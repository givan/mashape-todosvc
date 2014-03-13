package com.mashape.todosvc;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class TodoApp extends ResourceConfig {
    public TodoApp() {
        super(
                TodoResource.class,
                JacksonFeature.class
        );
    }
}