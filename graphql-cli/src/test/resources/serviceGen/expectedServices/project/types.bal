import ballerina/graphql;

type SchemaDocs11Api service object {
    *graphql:Service;
    # Fetch all the books from database
    resource function get books() returns Book?[]?;
    # Fetch a book by its id
    # + id - The id of the book to fetch
    resource function get book(int id) returns Book?;
};

# Represents a book written by an author
service class Book {
    # The id of the book, unique identifier
    resource function get id() returns int {
    }
    # The title of the book
    # # Deprecated
    # Use `name` instead
    @deprecated
    resource function get title() returns string {
    }
    resource function get name() returns string {
    }
}
