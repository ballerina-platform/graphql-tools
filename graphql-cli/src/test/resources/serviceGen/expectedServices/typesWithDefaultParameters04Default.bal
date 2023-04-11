import ballerina/graphql;

public type SchemaWithDefaultParameters04Api service object {
    *graphql:Service;

    resource function get book(int id) returns Book?;
	remote function createBook(CreateBookInput input = { id: 1, title: "no title"}) returns Book?;
};

public type CreateBookInput record {|
    int id;
    string title;
|};

public distinct service class Book {
	resource function get id() returns int {}
	resource function get title() returns string {}
}
