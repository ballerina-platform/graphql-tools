import ballerina/graphql;

configurable int port1 = 9099;

service SchemaWithAddedNewQueryFieldsApi on new graphql:Listener(port1) {
    resource function get book(int id, string? title) returns Book? {
        return new Book();
    }

    resource function get bookById(int id) returns Book? {
    }

    resource function get books() returns Book[]? {
    }
}
