import ballerina/graphql;

type SchemaDocsWithUnionApi service object {
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

# It can be either a student or a teacher
public type Profile Student|Teacher;

public distinct service class Student {
    resource function get id() returns int|error {
        return error("Not implemented");
    }

    resource function get name() returns string|error {
        return error("Not implemented");
    }
}

public distinct service class Teacher {
    resource function get id() returns int|error {
        return error("Not implemented");
    }

    resource function get name() returns string|error {
        return error("Not implemented");
    }
}
