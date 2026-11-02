import ballerina/graphql;

type SchemaWithInputsApiService service object {
    *graphql:Service;
    resource function get book(int id, string? title) returns Book?|error;
    resource function get books() returns Book?[]?|error;
    resource function get authors() returns Author[]|error;
    remote function createBook(CreateBookInput input) returns Book|error;
    remote function createAuthor(CreateAuthorInput? input) returns Author?|error;
};

public distinct service class Author {
    resource function get id() returns int|error {
        return error("Not implemented");
    }

    resource function get name() returns string|error {
        return error("Not implemented");
    }
}

public distinct service class Book {
    resource function get id() returns int|error {
        return error("Not implemented");
    }

    resource function get title() returns string|error {
        return error("Not implemented");
    }
}

public type CreateAuthorInput record {|
    string name;
|};

public type CreateBookInput record {|
    string title;
    int? authorId;
|};
