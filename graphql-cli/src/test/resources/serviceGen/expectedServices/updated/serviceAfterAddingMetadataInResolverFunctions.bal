import ballerina/graphql;

configurable int port = 9090;

service SchemaWithAddedMetadataInResolverFunctionsApi on new graphql:Listener(port) {
    # Fetch book by its id and title
    # + id - represents id of a book
    # + title - represents title of a book
    isolated resource function get book(int id, string? title) returns Book? {
        return new Book();
    }

    # Fetch book only by id
    isolated resource function get bookById(int id) returns Book? {
    }

    # Fetch books
    isolated resource function get books() returns Book[]? {
    }

    # Insert a book
    isolated remote function addBook(int id) returns Book {
    }

    # Fetch book titles in a stream
    resource function subscribe bookTitles() returns stream<string> {
    }
}
