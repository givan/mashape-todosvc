package com.mashape.todosvc;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.mashape.todosvc.model.Todo;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.ObjectMapper;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TodoResourceTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(
                TodoResource.class,
                JacksonFeature.class
        );
    }

    @Test
    public void testPostTodo() throws IOException {
        Todo newTodo = new Todo("1", "my todo", "my todo content", false);
        ObjectMapper mapper = new ObjectMapper();
        final Response result =
                target().path("/todos/todo")
                        .request(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(mapper.writeValueAsString(newTodo), MediaType.APPLICATION_JSON_TYPE));

        assertEquals(200, result.getStatus());
    }

    @Test
    public void testGetTodo() throws IOException {

        Todo newTodo = new Todo("1", "my todo", "my todo content", false);
        ObjectMapper mapper = new ObjectMapper();
        final Todo createdTodo =
                target().path("/todos/todo")
                        .request(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(mapper.writeValueAsString(newTodo), MediaType.APPLICATION_JSON_TYPE))
                        .readEntity(Todo.class);

        final Todo foundTodo =
                target().path("/todos/todo/" + String.valueOf(createdTodo.getId()))
                        .request(MediaType.APPLICATION_JSON)
                        .get(Todo.class);

        assertEquals(createdTodo.getId(), foundTodo.getId());
    }

    @Test
    public void testDeleteTodo() throws IOException {

        Todo newTodo = new Todo("1", "my todo", "my todo content", false);
        ObjectMapper mapper = new ObjectMapper();
        final Todo createdTodo =
                target().path("/todos/todo")
                        .request(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(mapper.writeValueAsString(newTodo), MediaType.APPLICATION_JSON_TYPE))
                        .readEntity(Todo.class);

        final Response response =
                target().path("/todos/todo/" + String.valueOf(createdTodo.getId()))
                        .request()
                        .delete();

        assertEquals(200, response.getStatus());
    }

    @Test
    public void testCompleteTodo() throws IOException {

        Todo newTodo = new Todo("1", "my todo", "my todo content", false);
        ObjectMapper mapper = new ObjectMapper();
        final Todo createdTodo =
                target().path("/todos/todo")
                        .request(MediaType.APPLICATION_JSON)
                        .post(Entity.entity(mapper.writeValueAsString(newTodo), MediaType.APPLICATION_JSON_TYPE))
                        .readEntity(Todo.class);

        createdTodo.setCompleted();

        final Response response =
                target().path("/todos/todo/" + String.valueOf(createdTodo.getId()))
                        .request()
                        .put(Entity.entity(mapper.writeValueAsString(createdTodo), MediaType.APPLICATION_JSON_TYPE));
        assertEquals(200, response.getStatus());

        final Todo foundTodo =
                target().path("/todos/todo/" + String.valueOf(createdTodo.getId()))
                        .request(MediaType.APPLICATION_JSON)
                        .get(Todo.class);

        assertTrue(foundTodo.getCompleted());
    }
}
