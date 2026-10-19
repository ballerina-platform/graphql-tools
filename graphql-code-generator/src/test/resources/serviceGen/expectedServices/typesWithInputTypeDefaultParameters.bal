import ballerina/graphql;

type SchemaWithInputTypeDefaultParametersApi service object {
    *graphql:Service;
    resource function get book(int id, string? title) returns Book?|error;
    remote function createBook(CreateBookInput input) returns Book|error;
};

public distinct service class Book {
    resource function get id() returns int|error {
        return error("Not implemented");
    }

    resource function get title() returns string|error {
        return error("Not implemented");
    }

    resource function get price() returns float|error {
        return error("Not implemented");
    }
}

public type CreateBookInput record {|
    string title = "No Title";
    int? authorId = 1;
    float price = 50.0;
|};
