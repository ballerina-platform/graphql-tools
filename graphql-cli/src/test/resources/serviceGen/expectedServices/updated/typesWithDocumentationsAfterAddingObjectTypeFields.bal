import ballerina/graphql;

type SchemaWithAddedObjectTypeFieldsIntoTypesWithDocumentationsApi service object {
    *graphql:Service;
    # Fetch a book by id and title
    # + id - The id of the book to fetch
    # + title - The title of the book to fetch
    resource function get book(int id, string? title) returns Book?;
    # Fetch all books
    resource function get books() returns Book[]?;
    # Fetch all authors
    resource function get authors() returns Author[]?;
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

# Contact information of a person
public type ContactInfo distinct service object {
    resource function get email() returns string;
    # # Deprecated
    # no longer used, use email.
    @deprecated
    resource function get name() returns string;
};

# Represents availability of a book
public enum Availability {
    AVAILABLE,
    BORROWED,
    # # Deprecated
    # no longer used
    @deprecated
    UNAVAILABLE,
    LOST
}

# It can be either a student or an author
public type Profile Student|Author;

# Represents an author of a book
# Person who writes books
public distinct service class Author {
    *ContactInfo;
    # The name of the author
    resource function get name() returns string {
        return "J. K. Rowling";
    }

    # The email of the author
    resource function get email() returns string {
        return "rowlingjk@gmail.com";
    }
}

# Represents a book written by an author
public distinct service class Book {
    # The id of the book, unique identifier
    resource function get id() returns int {
        return 1;
    }

    # The title of the book
    # # Deprecated
    # Use `name` instead
    @deprecated
    resource function get title() returns string {
        return "Harry Potter";
    }

    # The name of the book
    resource function get name() returns string {
        return "Harry Potter";
    }
}

# Represents a student
public distinct service class Student {
    # The name of the student
    resource function get name() returns string {
        return "John Doe";
    }

    # The email of the student
    resource function get email() returns string {
        return "doe@gmail.com";
    }
}
