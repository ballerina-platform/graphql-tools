import ballerina/graphql;

type SchemaDocsWithInputsApi service object {
    *graphql:Service;
    # Fetch all the books from database
    resource function get books() returns Book?[]?;
    # Fetch a book by its id
    # + id - The id of the book to fetch
    resource function get book(int id) returns Book?;
    # Add a new book to the database
    # + bookInfo - The book info to add
    remote function addBook(BookInfo bookInfo) returns Book?;
};

# Represents new book information
public type BookInfo record {|
    # The title of the book
    string title;
    # The author of the book
    # # Deprecated
    # No longer needed
    @deprecated
    string author;
|};

# Represents a book written by an author
public distinct service class Book {
    # The id of the book, unique identifier
    resource function get id() returns int {
    }

    # The title of the book
    resource function get title() returns string {
    }
}
