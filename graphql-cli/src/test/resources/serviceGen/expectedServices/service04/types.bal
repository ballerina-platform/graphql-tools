import ballerina/graphql;

type Schema04Api service object {
    *graphql:Service;

    resource function get book(int id, string? title) returns Book?;
	resource function get books() returns Book?[]?;
	resource function get authors() returns Author[];
	remote function addBook(string title, int authorId) returns Book?;
};

service class Author {
    resource function get id() returns int {}
    resource function get name() returns string {}
}

service class Book {
	resource function get id() returns int {}
	resource function get title() returns string {}
}



