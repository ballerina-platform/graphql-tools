import ballerina/graphql;
import ballerina/io;

configurable int port = 9090;

service SchemaWithAddedNewQueryFieldsApi on new graphql:Listener(port) {
    resource function get book(int id, string? title) returns Book? {
        io:println("get book");
        return new Book();
    }
}
