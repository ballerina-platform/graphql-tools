import ballerina/graphql;

public type SchemaDocs10Api service object {
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
};

public type ExamInfo distinct service object {
    # Get a student is passed or not
    # + marks - The marks student achieved
    resource function get pass(int marks) returns boolean;
};

# General Information of a person
public type Info distinct service object {
    # The name of the person
    resource function get name() returns string;
};

# Represents a book written by an author
public distinct service class Book {
    # The id of the book, unique identifier
	resource function get id() returns int {}
	# The title of the book
	resource function get title() returns string {}
}

public distinct service class Student {
    *ContactInfo;
    *Info;
    *ExamInfo;

    # The id of the student, unique identifier
    resource function get id() returns int {}
    # The name of the student
    resource function get name() returns string {}
    # The email of the student
    resource function get email() returns string {}
    resource function get pass(int marks) returns boolean {}
}
