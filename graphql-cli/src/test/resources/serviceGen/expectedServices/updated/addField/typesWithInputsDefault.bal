import ballerina/graphql;

type SchemaWithInputsApi service object {
    *graphql:Service;
    resource function get book(int id, string? title) returns Book?;
    resource function get books() returns Book?[]?;
    resource function get authors() returns Author[];
    remote function createBook(CreateBookInput input) returns Book;
    remote function createAuthor(CreateAuthorInput? input) returns Author?;
};

public type CreateAuthorInput record {|
    string name;
    string email;
    string address;
    int age = 18;
|};

public type CreateBookInput record {|
    string title;
    int? authorId;
    float price;
    string version;
|};

public distinct service class Author {
    resource function get id() returns int {
        return 1;
    }

    resource function get name() returns string {
        return "John";
    }
}

public distinct service class Book {
    resource function get id() returns int {
        return 10;
    }

    resource function get title() returns string {
        return "Harry Potter";
    }
}