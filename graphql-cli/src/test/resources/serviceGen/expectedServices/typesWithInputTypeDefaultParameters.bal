import ballerina/graphql;

type SchemaWithInputTypeDefaultParametersApi service object {
    *graphql:Service;
    resource function get book(int id, string? title) returns Book?;
    remote function createBook(CreateBookInput input) returns Book;
};

public type CreateBookInput record {|
    string title = "No Title";
    int? authorId = 1;
    float price = 50.0;
|};

public distinct service class Book {
    resource function get id() returns int {
    }

    resource function get title() returns string {
    }

    resource function get price() returns float {
    }
}
