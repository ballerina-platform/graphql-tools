import ballerina/graphql;

configurable int port = 9090;

service SchemaWithSingleObjectApi on new graphql:Listener(port) {
    resource function get book(int id, string? title) returns Book?|error {
        return error("Not implemented");
    }

    resource function get books() returns Book[]?|error {
        return error("Not implemented");
    }
}
