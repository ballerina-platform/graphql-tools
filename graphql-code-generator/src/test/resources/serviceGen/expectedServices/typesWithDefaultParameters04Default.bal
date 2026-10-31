import ballerina/graphql;

type SchemaWithDefaultParameters04ApiService service object {
    *graphql:Service;
    resource function get book(int id) returns Book?|error;
    remote function createBookWithAuthor(CreateBookInput input = {id: 1, title: "no title", author: {id: 1, name: "no name"}}) returns Book?|error;
};

public distinct service class Book {
    resource function get id() returns int|error {
        return error("Not implemented");
    }

    resource function get title() returns string|error {
        return error("Not implemented");
    }
}

public type CreateAuthorInput record {|
    int id;
    string name;
|};

public type CreateBookInput record {|
    int id;
    string title;
    CreateAuthorInput? author;
|};
