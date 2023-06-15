import ballerina/graphql;

type SchemaWithAddedNewQueryFieldsApi service object {
    *graphql:Service;
    resource function get book(int id, string? title) returns Book?;
};

public distinct service class Book {
    resource function get id() returns int {
        return 1;
    }

    resource function get title() returns string {
        return "Harry Potter";
    }

    function findBookCount() returns int {
        return 1;
    }
}
