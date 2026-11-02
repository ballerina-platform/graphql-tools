import ballerina/graphql;

type SchemaDocsWithDeprecated01ApiService service object {
    *graphql:Service;
    # Fetch all the books from database
    resource function get books() returns Book?[]?|error;
    # Fetch a book by its id
    # + id - The id of the book to fetch
    resource function get book(int id) returns Book?|error;
};

# Represents a book written by an author
public distinct service class Book {
    # The id of the book, unique identifier
    resource function get id() returns int|error {
        return error("Not implemented");
    }

    # The title of the book
    # # Deprecated
    # Use `name` instead
    @deprecated
    resource function get title() returns string|error {
        return error("Not implemented");
    }

    resource function get name() returns string|error {
        return error("Not implemented");
    }
}
