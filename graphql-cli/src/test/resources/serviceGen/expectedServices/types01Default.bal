import ballerina/graphql;

type Schema01Api service object {
    *graphql:Service;

    resource function get book(int id, string? title) returns Book?;
	resource function get books() returns Book[]?;
};

service class Book {
	resource function get id() returns int {}
	resource function get title() returns string {}
}

