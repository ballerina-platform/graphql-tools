import ballerina/graphql;

type SchemaDocsWithDeprecated03Api service object {
    *graphql:Service;

    # Fetch all the books from database
    resource function get books() returns Book?[]?;
    # Fetch a book by its id
    # + id - The id of the book to fetch
    resource function get book(int id) returns Book?;
};

# Contact information of a person
public type ContactInfo distinct service object {
    # The email of the person
    resource function get email() returns string;
    # The name of the person
    # # Deprecated
    # no longer used, use email.
    @deprecated
    resource function get name() returns string;
};

# Represents a book written by an author
public distinct service class Book {
    # The id of the book, unique identifier
    resource function get id() returns int {}
    # The title of the book
    resource function get title() returns string {}
}
