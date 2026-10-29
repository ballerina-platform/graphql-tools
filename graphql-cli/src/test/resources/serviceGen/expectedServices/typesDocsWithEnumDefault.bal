import ballerina/graphql;

type SchemaDocsWithEnumApiService service object {
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
    resource function get title() returns string|error {
        return error("Not implemented");
    }
}

# Availability of the book
public enum Status {
    # The book is available
    AVAILABLE,
    # The book is not available
    NOT_AVAILABLE
}
