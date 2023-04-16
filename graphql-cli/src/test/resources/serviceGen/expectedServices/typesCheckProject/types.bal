import ballerina/graphql;

type SchemaWithDefaultParameters04Api service object {
    *graphql:Service;
    resource function get book(int id) returns Book?;
    remote function createBook(CreateBookInput input = {id: 1, title: "no title"}) returns Book?;
    remote function createBookWithAuthor(CreateBookInput input = {id: 1, title: "no title", author: {id: 1, name: "no name"}}) returns Book?;
};

public type CreateAuthorInput record {|
    int id;
    string name;
|};

public type CreateBookInput record {|
    int id;
    string title;
    CreateAuthorInput? author;
|};

public distinct service class Book {
    resource function get id() returns int {
    }
    resource function get title() returns string {
    }
}
