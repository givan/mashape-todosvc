package com.mashape.todosvc.mongodb.client;

import com.mashape.todosvc.model.Todo;
import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoURI;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.WriteResult;

import java.math.BigInteger;
import java.net.UnknownHostException;
import java.security.SecureRandom;

/**
 * Created by georgei on 3/15/14.
 */
public class TodoMongoDb {
    public final String TodoDbLocalhost = "mongodb://127.0.0.1:27017/todo";
    private DB mongoDB;
    private SecureRandom random = new SecureRandom();

    public TodoMongoDb() throws UnknownHostException {
        MongoURI mongolabUri =
                new MongoURI(System.getenv("MONGOLAB_URI") != null ? System.getenv("MONGOLAB_URI") : TodoDbLocalhost);
        Mongo m = new Mongo(mongolabUri);
        mongoDB = m.getDB(mongolabUri.getDatabase());
        if ((mongolabUri.getUsername() != null) && (mongolabUri.getPassword() != null)) {
            mongoDB.authenticate(mongolabUri.getUsername(), mongolabUri.getPassword());
        }
    }

    private JacksonDBCollection<Todo, String> getJacksonDBCollection() {
        return JacksonDBCollection.wrap(
                mongoDB.getCollection(Todo.class.getSimpleName().toLowerCase()),
                Todo.class, String.class);
    }

    private String generateNewTodoId() {
        return new BigInteger(130, random).toString(32);
    }

    public boolean Add(Todo newTodo) {
        // the new id will be passed back in the newTodo object so the clients can refer to it
        newTodo.setId( generateNewTodoId() );

        WriteResult<Todo, String> result = getJacksonDBCollection().insert(newTodo);

        return result.getError() == null;
    }

    public Todo FindById(String todoId) {
        Todo foundTodo = getJacksonDBCollection().findOneById(todoId);

        return foundTodo;
    }

    public boolean Update(Todo newTodo) {
        WriteResult<Todo, String> result = getJacksonDBCollection().updateById(newTodo.getId(), newTodo);

        return result.getError() == null;
    }

    public boolean delete(String todoId) {

        WriteResult<Todo, String> result = getJacksonDBCollection().removeById(todoId);

        return result.getError() == null;
    }
}
