import ballerina/graphql;

type SchemaWithListsApiService service object {
    *graphql:Service;
    resource function get book(int id, string? title) returns Book?|error;
    resource function get booksPattern1() returns Book?[]?|error;
    resource function get booksPattern2() returns Book[]|error;
    resource function get booksPattern3() returns Book?[]|error;
    resource function get booksPattern4() returns Book[]?|error;
};

public distinct service class Book {
    resource function get id() returns int|error {
        return error("Not implemented");
    }

    resource function get title() returns string|error {
        return error("Not implemented");
    }
}
