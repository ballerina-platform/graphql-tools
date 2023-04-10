import ballerina/graphql;

public type SchemaWithListsApi service object {
    *graphql:Service;

    resource function get book(int id, string? title) returns Book?;
	resource function get booksPattern1() returns Book?[]?;
	resource function get booksPattern2() returns Book[];
	resource function get booksPattern3() returns Book?[];
	resource function get booksPattern4() returns Book[]?;
};

public distinct service class Book {
	resource function get id() returns int {}
	resource function get title() returns string {}
}
