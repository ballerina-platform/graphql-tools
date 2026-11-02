import ballerina/graphql;

configurable int port = 9090;

service SchemaDocsWithMutationAndSubscriptionResolversApiService on new graphql:Listener(port) {
    # Fetch a book by its id
    resource function get book(int id) returns Book?|error {
        return error("Not implemented");
    }

    # Fetch all the books
    resource function get books() returns Book?[]?|error {
        return error("Not implemented");
    }

    # Create a new book
    remote function createBook(string title) returns Book?|error {
        return error("Not implemented");
    }

    # Create a new author
    remote function createAuthor(string name) returns Author?|error {
        return error("Not implemented");
    }

    # Get stream of book titles
    resource function subscribe bookTitles() returns stream<string>|error {
        return error("Not implemented");
    }
}
