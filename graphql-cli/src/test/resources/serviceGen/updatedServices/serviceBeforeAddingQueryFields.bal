import ballerina/graphql;

configurable int port = 9090;

service SchemaWithAddedNewQueryFieldsApi on new graphql:Listener(port) {
    resource function get book(int id, string? title) returns Book? {
        return new Book();
    }
}
