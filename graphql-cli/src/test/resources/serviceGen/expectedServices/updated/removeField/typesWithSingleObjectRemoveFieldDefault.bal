import ballerina/graphql;

type SchemaWithSingleObjectRemoveFieldApi service object {
    *graphql:Service;
    resource function get book(int id, string? title) returns Book?;
    resource function get books() returns Book[]?;
};

public distinct service class Book {
    resource function get id() returns int {
        return 1;
    }
}