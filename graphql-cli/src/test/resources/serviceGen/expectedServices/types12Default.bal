import ballerina/graphql;

public type Schema12Api service object {
    *graphql:Service;

    resource function get book(int id, string? title) returns Book?;
	resource function get books() returns Book?[]?;
	resource function get booksOfBooks() returns Book?[]?[]?;
	resource function get booksOfBooksOfBooks() returns Book?[]?[]?[]?;
};

public service class Book {
	resource function get id() returns int {}
	resource function get title() returns string {}
}
