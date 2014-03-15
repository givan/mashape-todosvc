package com.mashape.todosvc;

import com.mashape.todosvc.clients.TodoCompletedSms;
import com.mashape.todosvc.model.Todo;
import com.mashape.todosvc.clients.TodoMongoDb;
import com.mashape.todosvc.clients.SearchboxClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Root resource (exposed at "todo" path)
 */
@Path("todos")
public class TodoResource {

    private final TodoCompletedSms twilioSms;
    private final SearchboxClient searchBoxClient;
    private final TodoMongoDb todoMongoDb;

    public TodoResource() throws Exception {
        // use spring framework for DI instead of newing up objects here..
        this.searchBoxClient = new SearchboxClient();
        this.todoMongoDb = new TodoMongoDb();
        this.twilioSms = new TodoCompletedSms();
    }

    @Path("/todo")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Todo CreateTodo(Todo newTodo) throws Exception {

        boolean isSuccess = false;

        newTodo.setId(null);// invalidate the id (if set); we want to generate a new id

        isSuccess = this.todoMongoDb.add(newTodo);

        if (isSuccess) {
            isSuccess = this.searchBoxClient.insert(newTodo);
        }

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

        Todo foundTodo = this.todoMongoDb.findById(todoId);

        return foundTodo;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Todo> GetTodos() throws Exception {
        return this.todoMongoDb.getAll();
    }

    @Path("/todo/{todoId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @PUT
    public Response UpdateTodo(@PathParam("todoId") String todoId, Todo updateTodo) throws Exception {
        updateTodo.setId(todoId); // use the id from the path

        boolean isSuccess = this.todoMongoDb.update(updateTodo);

        if (isSuccess) {
            isSuccess = this.searchBoxClient.upsert(updateTodo);
        }

        /*
HTTP 400 "Bad Request"
The Web server detected an error in the protocol data received from the client. Normally this indicates a technical
glitch involving the client, but the error may also be caused by data corruption on the network itself.

HTTP 204 "No Content"
The server sent a valid reply to a client request that contains header information only (
i.e., does not contain any message body). Web clients can use HTTP 204 to process server responses more efficiently,
avoiding refreshing pages unnecessarily, for example.

I can return 400 here as well, but i like the idea that 204 will not cause the browser unnecessary refresh
          * */
        int httpStatus = (isSuccess) ? 200 : 204; // 204 denotes no content - indication for an invalid todoId

        return Response.status(httpStatus).build();
    }

    @Path("/todo/{todoId}/completed")
    @PUT
    public Response Completed(@PathParam("todoId") String todoId, @QueryParam("fromPhoneNumber") String fromPhoneNumber)
            throws Exception {
        Todo updateCompleted = new Todo(todoId, null, null, true);

        boolean isSuccess = this.todoMongoDb.update(updateCompleted);

        if (isSuccess) {
            isSuccess = this.searchBoxClient.upsert(updateCompleted);

            // finally if the fromPhoneNumber was specified, try to send a sms to our server
            // using the provided fromPhoneNumber which identifies WHO completed the TODO
            if (fromPhoneNumber != null && fromPhoneNumber.length() > 0) {
                isSuccess = this.twilioSms.send(updateCompleted, fromPhoneNumber);
            }
        }

        int httpStatus = (isSuccess) ? 200 : 204; // 204 denotes no content - indication for an invalid todoId

        return Response.status(httpStatus).build();
    }

    @Path("/todo/{todoId}")
    @DELETE
    public Response DeleteTodo(@PathParam("todoId") String todoId) throws Exception {

        boolean isSuccess = this.todoMongoDb.delete(todoId);

        if (isSuccess) {
            isSuccess = this.searchBoxClient.delete(todoId);
        }

        int httpStatus = (isSuccess) ? 200 : 204; // 204 denotes no content - indication for an invalid todoId

        return Response.status(httpStatus).build();
    }
}
