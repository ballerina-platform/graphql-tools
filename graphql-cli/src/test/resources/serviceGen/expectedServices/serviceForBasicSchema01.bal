import ballerina/graphql;

configurable int port = 9090;

service SchemaWithBasic01Api on new graphql:Listener(port) {
    resource function get book(int id, string? title) returns Book? {}
    resource function get books() returns Book[]? {}
}
