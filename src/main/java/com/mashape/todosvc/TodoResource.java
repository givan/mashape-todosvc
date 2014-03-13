package com.mashape.todosvc;

import com.mashape.todosvc.model.Todo;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;


/**
 * Root resource (exposed at "myresource" path)
 */
@Path("myresource")
public class TodoResource {

    private static List<Todo> todos = new ArrayList<Todo>();

    public TodoResource() {
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response CreateTodo(Todo newTodo) {
        todos.add(newTodo);
        return Response.status(201).entity("OK").build();
    }

    @Path("/{todoId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Todo GetTodo(@PathParam("todoId") int todoId) {
        return todos.get(todoId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Todo> GetTodos() {
        return todos;
    }

    @GET
    @Path("/title/{title}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response GetTodo(@PathParam("title") String title) {

        for(int i = 0; i < todos.size(); i++) {
            if (todos.get(i).getTitle() == title) {
                return Response.status(200).entity(todos.get(i)).build();
            }
        }

        return Response.status(400).entity("NotFound: " + title).build();
    }

    @Path("/{todoId}/completed")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    public Response Completed(@PathParam("todoId") int todoId) {
        if (0 <= todoId && todos.size() > todoId) {
            todos.get(todoId).setCompleted();
            return Response.status(200).build();
        }
        else {
            return Response.status(400).entity("NotFound: " + todoId).build();
        }
    }
}
