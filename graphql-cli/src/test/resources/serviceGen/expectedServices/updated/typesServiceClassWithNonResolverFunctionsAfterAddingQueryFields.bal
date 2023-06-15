import ballerina/graphql;

type SchemaWithAddedNewQueryFieldsApi service object {
    *graphql:Service;
    resource function get book(int id, string? title) returns Book?;
    resource function get bookById(int id) returns Book?;
    resource function get books() returns Book[]?;
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
