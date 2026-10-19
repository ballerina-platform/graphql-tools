import ballerina/graphql;

type SchemaWithSingleObjectApi service object {
    *graphql:Service;
    resource function get book(int id, string? title) returns Book?|error;
    resource function get books() returns Book[]?|error;
};

public distinct service class Book {
    resource function get id() returns int|error {
        return error("Not implemented");
    }

    resource function get title() returns string|error {
        return error("Not implemented");
    }
}
