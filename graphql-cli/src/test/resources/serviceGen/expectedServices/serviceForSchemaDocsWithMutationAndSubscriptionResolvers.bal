import ballerina/graphql;

configurable int port = 9090;

service SchemaDocsWithMutationAndSubscriptionResolversApi on new graphql:Listener(port) {
    # Fetch a book by its id
    resource function get book(int id) returns Book? {
    }

    # Fetch all the books
    resource function get books() returns Book?[]? {
    }

    # Create a new book
    remote function createBook(string title) returns Book? {
    }

    # Create a new author
    remote function createAuthor(string name) returns Author? {
    }

    # Get stream of book titles
    resource function subscribe bookTitles() returns stream<string> {
    }
}
