import ballerina/graphql;

type SchemaDocsWithMutationAndSubscriptionResolversApiService service object {
    *graphql:Service;
    # Fetch a book by its id
    resource function get book(int id) returns Book?|error;
    # Fetch all the books
    resource function get books() returns Book?[]?|error;
    # Create a new book
    remote function createBook(string title) returns Book?|error;
    # Create a new author
    remote function createAuthor(string name) returns Author?|error;
    # Get stream of book titles
    resource function subscribe bookTitles() returns stream<string>|error;
};

public distinct service class Author {
    resource function get id() returns int|error {
        return error("Not implemented");
    }

    resource function get name() returns string|error {
        return error("Not implemented");
    }
}

public distinct service class Book {
    resource function get id() returns int|error {
        return error("Not implemented");
    }

    resource function get title() returns string|error {
        return error("Not implemented");
    }
}
