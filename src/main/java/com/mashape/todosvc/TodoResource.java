package com.mashape.todosvc;

import com.mashape.todosvc.model.Todo;
import com.mashape.todosvc.mongodb.client.TodoMongoDb;
import com.mashape.todosvc.searchbox.io.client.SearchboxClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;


/**
 * Root resource (exposed at "todo" path)
 */
@Path("todos")
public class TodoResource {

    private SearchboxClient searchBoxClient = null;
    private TodoMongoDb todoMongoDb = null;

    public TodoResource() throws Exception {
        // use spring framework for DI instead of newing up objects here..
        this.searchBoxClient = new SearchboxClient();
        this.todoMongoDb = new TodoMongoDb();
    }

    @Path("/todo")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Todo CreateTodo(Todo newTodo) throws Exception {

        boolean isSuccess = false;

        newTodo.setId(null);// invalidate the id (if set); we want to generate a new id

        isSuccess = this.todoMongoDb.Add(newTodo);

        if (isSuccess) {
            isSuccess = this.searchBoxClient.insert(newTodo);
        }

        int httpStatus = (isSuccess) ? 200 : 400;

        return newTodo;
    }

    @Path("/search")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Todo> SearchTodos(@QueryParam("q") String query) throws Exception {

        List<Todo> foundTodos = this.searchBoxClient.search(query);

        return foundTodos;
    }

    @Path("/todo/{todoId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Todo GetTodo(@PathParam("todoId") String todoId) throws Exception {

        Todo foundTodo = this.searchBoxClient.findById(todoId);

        return foundTodo;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Todo> GetTodos() throws Exception {
        return this.searchBoxClient.getAll();
    }

    @Path("/todo/{todoId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @PUT
    public Response UpdateTodo(@PathParam("todoId") String todoId, Todo updateTodo) throws Exception {
        updateTodo.setId(todoId); // use the id from the path

        boolean isSuccess = this.todoMongoDb.Update(updateTodo);

        if (isSuccess) {
            isSuccess = this.searchBoxClient.upsert(updateTodo);
        }

        int httpStatus = (isSuccess) ? 200 : 400;

        return Response.status(httpStatus).build();
    }

    @Path("/todo/{todoId}/completed")
    @PUT
    public Response Completed(@PathParam("todoId") String todoId) throws Exception {
        Todo updateCompleted = new Todo(todoId, null, null, true);

        boolean isSuccess = this.todoMongoDb.Update(updateCompleted);

        if (isSuccess) {
            isSuccess = this.searchBoxClient.upsert(updateCompleted);
        }

        int httpStatus = (isSuccess) ? 200 : 400;

        return Response.status(httpStatus).build();
    }

    @Path("/todo/{todoId}")
    @DELETE
    public Response DeleteTodo(@PathParam("todoId") String todoId) throws Exception {

        boolean isSuccess = this.todoMongoDb.delete(todoId);

        if (isSuccess) {
            isSuccess = this.searchBoxClient.delete(todoId);
        }

        int httpStatus = (isSuccess) ? 200 : 400;

        return Response.status(httpStatus).build();
    }
}
