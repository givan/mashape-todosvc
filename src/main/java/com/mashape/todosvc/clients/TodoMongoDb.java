package com.mashape.todosvc.clients;

import com.mashape.todosvc.model.Todo;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoURI;
import com.mongodb.WriteConcern;
import net.vz.mongodb.jackson.DBCursor;
import net.vz.mongodb.jackson.DBUpdate;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.WriteResult;

import java.math.BigInteger;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class TodoMongoDb {
    public final String TodoDbLocalhost = "mongodb://127.0.0.1:27017/todo";
    private final JacksonDBCollection<Todo, String> todoCollection;
    private DB mongoDB;
    private SecureRandom random = new SecureRandom();

    public TodoMongoDb() throws UnknownHostException {
        // either run in heroku or use the localhost url
        MongoURI mongolabUri =
                new MongoURI(System.getenv("MONGOLAB_URI") != null ? System.getenv("MONGOLAB_URI") : TodoDbLocalhost);

        Mongo m = new Mongo(mongolabUri);
        mongoDB = m.getDB(mongolabUri.getDatabase());

        if ((mongolabUri.getUsername() != null) && (mongolabUri.getPassword() != null)) {
            mongoDB.authenticate(mongolabUri.getUsername(), mongolabUri.getPassword());
        }

        this.todoCollection = getJacksonDBCollection();
        this.todoCollection.setWriteConcern(WriteConcern.JOURNAL_SAFE);
    }

    public boolean add(Todo newTodo) {
        // the new id will be passed back in the newTodo object so the clients can refer to it
        newTodo.setId( generateNewTodoId() );

        WriteResult<Todo, String> result = this.todoCollection.insert(newTodo);

        boolean isSuccess = IsOperationSuccessful(result);

        return isSuccess;
    }

    public Todo findById(String todoId) {
        Todo foundTodo = this.todoCollection.findOneById(todoId);

        return foundTodo;
    }

    public List<Todo> getAll() {
        ArrayList<Todo> results = new ArrayList<Todo>();
        DBCursor<Todo> dbCursor = this.todoCollection.find();
        while(dbCursor.hasNext()) {
            results.add(dbCursor.next());
        }
        return results;
    }

    public boolean update(Todo newTodo) {
        DBUpdate.Builder update = DBUpdate.set("completed", newTodo.getCompleted());

        if (newTodo.getTitle() != null) {
            update.set("title", newTodo.getTitle());
        }

        if (newTodo.getBody() != null) {
            update.set("body", newTodo.getBody());
        }

        WriteResult<Todo, String> result = this.todoCollection.updateById(newTodo.getId(), update);
        boolean isSuccess = IsOperationSuccessful(result) && result.getN() == 1;

        return isSuccess;
    }

    public boolean delete(String todoId) {

        WriteResult<Todo, String> result = this.todoCollection.removeById(todoId);

        boolean isSuccess = IsOperationSuccessful(result) && result.getN() == 1;

        return isSuccess;
    }

    private JacksonDBCollection<Todo, String> getJacksonDBCollection() {
        return JacksonDBCollection.wrap(
                mongoDB.getCollection(
                        Todo.class.getSimpleName().toLowerCase()),
                        Todo.class, String.class);
    }

    private String generateNewTodoId() {
        return new BigInteger(130, random).toString(32);
    }

    private boolean IsOperationSuccessful(WriteResult<Todo, String> result) {
        return result.getLastError().ok();
    }
}
