import ballerina/graphql;

public type SchemaWithDefaultParameters02Api service object {
    *graphql:Service;

    resource function get book(int id, Availability? available = UNAVAILABLE) returns Book?;
	remote function addBook(string title, int authorId) returns Book?;
	resource function subscribe bookTitles() returns stream<string>;
};

public enum Availability {
    AVAILABLE,
    UNAVAILABLE
}

public distinct service class Book {
	resource function get id() returns int {}
	resource function get title() returns string {}
	resource function get price() returns float {}
}
