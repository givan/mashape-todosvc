package com.mashape.todosvc;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class TodoApp extends ResourceConfig {
    public TodoApp() {

        // TODO: consider adding IoC container (spring)
        // TODO: consider adding logging in the service
        // TODO: consider adding configuration management (loading up config)
        // TODO: consider using async patterns (currently is blocking)

        super(
                TodoResource.class,
                JacksonFeature.class
        );
    }
}