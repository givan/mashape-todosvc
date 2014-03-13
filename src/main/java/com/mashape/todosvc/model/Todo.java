package com.mashape.todosvc.model;

import org.codehaus.jackson.annotate.JsonProperty;

// POJO for to-do entity
public class Todo {
    @JsonProperty("title")
    private String title;

    @JsonProperty("body")
    private String body;

    @JsonProperty("completed")
    private Boolean completed;

    public Todo() {

    }

    public Todo(String title, String body, Boolean completed) {
        this.title = title;
        this.body = body;
        this.completed = completed;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public String getBody() {
        return body;
    }

    public String getTitle() {
        return title;
    }

    public void setCompleted() {
        this.completed = true;
    }
}
