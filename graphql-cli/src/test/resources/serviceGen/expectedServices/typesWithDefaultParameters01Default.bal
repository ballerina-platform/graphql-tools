import ballerina/graphql;

type SchemaWithDefaultParameters01Api service object {
    *graphql:Service;

    resource function get book(int id=1, string? title="no name", float? price=0.0, boolean? available = false) returns Book?;
	resource function get books() returns Book?[]?;
	remote function addBook(string title, int authorId) returns Book?;
	resource function subscribe bookTitles() returns stream<string>;
};

public distinct service class Author {
    resource function get id() returns int {}
    resource function get name() returns string {}
}

public distinct service class Book {
	resource function get id() returns int {}
	resource function get title() returns string {}
	resource function get price() returns float {}
}
