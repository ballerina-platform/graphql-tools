import ballerina/graphql;

type Schema03Api service object {
    *graphql:Service;
    resource function get book(int id, string? title) returns Book?;
    resource function get books() returns Book?[]?;
    resource function get authors() returns Author[];
    remote function createBook(CreateBookInput input) returns Book?;
    remote function createAuthor(CreateAuthorInput? input) returns Author?;
};

type CreateAuthorInput record {|
    string name;
|};

type CreateBookInput record {|
    string title;
    int? authorId;
|};

service class Author {
    resource function get id() returns int {
    }
    resource function get name() returns string {
    }
}

service class Book {
    resource function get id() returns int {
    }
    resource function get title() returns string {
    }
}
