import ballerina/graphql;

configurable int port = 9090;

service SchemaWithAddedMetadataInResolverFunctionsApi on new graphql:Listener(port) {
    isolated resource function get book(int id, string? title) returns Book? {
        return new Book();
    }

    isolated resource function get bookById(int id) returns Book? {
    }

    isolated resource function get books() returns Book[]? {
    }

    isolated remote function addBook(int id) returns Book {
    }

    resource function subscribe bookTitles() returns stream<string> {
    }
}
