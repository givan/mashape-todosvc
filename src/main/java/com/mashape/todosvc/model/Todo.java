package com.mashape.todosvc.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.searchbox.annotations.JestId;
import net.vz.mongodb.jackson.Id;
import org.codehaus.jackson.annotate.JsonProperty;

// POJO for to-do entity
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Todo {

    @Id
    @JestId
    @JsonProperty("id")
    private String id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("body")
    private String body;

    @JsonProperty("completed")
    private Boolean completed;

    public Todo() {

    }

    public Todo(String id, String title, String body, Boolean completed) {
        // TODO - think about validation here - what if the title is too long, what if the body is too long?

        this.id = id;
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

    @Override
    public String toString() {
        return "Todo {title=" + this.title + ", body=" + this.body + ", completed=" + this.completed + "}";
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

}
