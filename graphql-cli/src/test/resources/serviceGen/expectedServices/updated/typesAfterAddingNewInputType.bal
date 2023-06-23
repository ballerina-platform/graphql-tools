import ballerina/graphql;

type SchemaWithAddedNewInputTypeApi service object {
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
|};

public type CreateBookInput record {|
    string title;
    int? authorId;
    float price;
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

public type CreateStoryInput record {|
    string name;
|};
