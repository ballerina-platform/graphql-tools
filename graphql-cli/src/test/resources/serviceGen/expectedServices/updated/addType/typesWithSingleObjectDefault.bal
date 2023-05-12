import ballerina/graphql;

type SchemaWithSingleObjectApi service object {
    *graphql:Service;
    resource function get book(int id, string? title) returns Book?;
    resource function get books() returns Book[]?;
};

public distinct service class Book {
    resource function get id() returns int {
        return 1;
    }

    resource function get title() returns string {
        return "Harry Potter";
    }
}

public distinct service class Author {
    resource function get id() returns int {
    }

    resource function get name() returns string {
    }
}