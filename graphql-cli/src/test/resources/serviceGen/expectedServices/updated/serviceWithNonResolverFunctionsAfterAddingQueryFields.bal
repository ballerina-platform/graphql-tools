import ballerina/graphql;

configurable int port = 9090;

service SchemaWithAddedNewQueryFieldsApi on new graphql:Listener(port) {
    function findBookCount() returns int {
        return 10;
    }

    resource function get book(int id, string? title) returns Book? {
        return new Book();
    }

    resource function get bookById(int id) returns Book? {
    }

    resource function get books() returns Book[]? {
    }
}
