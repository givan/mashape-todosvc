package com.mashape.todosvc.searchbox.io.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mashape.todosvc.model.Todo;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.*;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.List;

/**
 * Created by georgei on 3/14/14.
 */
public class SearchboxClient {
    public static final String TODOS_INDEX = "todos";
    public static final String TODO_TYPE = "todo";

    // TODO: this should go in a config file
    private final String SearchBoxUrl = "http://bombur-us-east-1.searchly.com/api-key/415a0534d9c1bca9f626744c54aac4b2";

    private JestClient searchBoxClient = null;

    public SearchboxClient() {
        // Configuration
        HttpClientConfig clientConfig =
                new HttpClientConfig.Builder(SearchBoxUrl)
                        .multiThreaded(true)
                        .build();

        // Construct a new Jest client according to configuration via factory
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(clientConfig);
        this.searchBoxClient = factory.getObject();
    }

    public List<Todo> getAll() throws Exception {
        // just request *all* todos
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        Search search = new Search.Builder(searchSourceBuilder.toString())
                .addIndex(TODOS_INDEX)
                .build();

        JestResult result = this.searchBoxClient.execute(search);
        return result.getSourceAsObjectList(Todo.class);
    }

    public List<Todo> search(String query) throws Exception {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.multiMatchQuery(query, "title^5", "body"));

        Search search = new Search.Builder(searchSourceBuilder.toString())
                // multiple index or types can be added.
                .addIndex(TODOS_INDEX)
                .build();

        JestResult result = this.searchBoxClient.execute(search);
        List<Todo> foundTodos = result.getSourceAsObjectList(Todo.class);
        return foundTodos;
    }

    public boolean insert(Todo newTodo) throws Exception {
        Index index = new Index.Builder(newTodo).index(TODOS_INDEX).type(TODO_TYPE).build();
        JestResult result = this.searchBoxClient.execute(index);
        return result.isSucceeded();
    }

    public Todo findById(String id) throws Exception {
        Get get = new Get.Builder(TODOS_INDEX, String.valueOf(id)).type(TODO_TYPE).build();

        JestResult result = this.searchBoxClient.execute(get);

        Todo foundTodo = result.getSourceAsObject(Todo.class);
        return foundTodo;
    }

    public boolean update(Todo newTodo) throws Exception {
        boolean isUpdated = false;

        // first retrieve the old todo by id
        Todo existingTodo = findById(newTodo.getId());

        if (existingTodo != null) {

            String title = (newTodo.getTitle() != null) ? newTodo.getTitle() : existingTodo.getTitle();
            String body = (newTodo.getBody() != null) ? newTodo.getBody() : existingTodo.getBody();
            boolean completed = (existingTodo.getCompleted() || newTodo.getCompleted());
            Todo mergedTodo = new Todo(newTodo.getId(), title, body, completed);

            ObjectMapper mapper = new ObjectMapper();
            String mergedTodoJson = mapper.writeValueAsString(mergedTodo);

            String script = "{\n" +
                    "    \"doc_as_upsert\" : \"true\",\n" +
                    "    \"doc\" : " + mergedTodoJson +
                    "}";

            JestResult result = this.searchBoxClient.execute(
                    new Update.Builder(script).index(TODOS_INDEX).type(TODO_TYPE).id(String.valueOf(newTodo.getId())
                    ).build());
            isUpdated = result.isSucceeded();
        }

        return isUpdated;
    }

    public boolean upsert(Todo newTodo) throws Exception {
        boolean isUpdated = false;

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        String newTodoJson = mapper.writeValueAsString(newTodo);

        String script = "{\n" +
                "    \"doc_as_upsert\" : \"true\",\n" +
                "    \"doc\" : " + newTodoJson +
                "}";

        JestResult result = this.searchBoxClient.execute(
                new Update.Builder(script).index(TODOS_INDEX).type(TODO_TYPE).id(String.valueOf(newTodo.getId())
                ).build());
        isUpdated = result.isSucceeded();

        return isUpdated;
    }

    public boolean delete(String todoId) throws Exception {
        JestResult result = this.searchBoxClient.execute(new Delete.Builder(todoId)
                .index(TODOS_INDEX)
                .type(TODO_TYPE)
                .build());
        return result.isSucceeded();
    }
}
