import ballerina/graphql;

public type SchemaDocs08Api service object {
    *graphql:Service;

    # Fetch all the books from database
	resource function get books() returns Book?[]?;
	# Fetch a book by its id
	# + id - The id of the book to fetch
    resource function get book(int id) returns Book?;
};

# Availability of the book
public enum Status {
    # The book is available
    AVAILABLE,
    # The book is not available
    NOT_AVAILABLE
}

# Represents a book written by an author
public distinct service class Book {
    # The id of the book, unique identifier
	resource function get id() returns int {}
	# The title of the book
	resource function get title() returns string {}
}