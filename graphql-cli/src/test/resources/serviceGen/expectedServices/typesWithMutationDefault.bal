import ballerina/graphql;

type SchemaWithMutationApi service object {
    *graphql:Service;
    resource function get book(int id, string? title) returns Book?;
    resource function get books() returns Book?[]?;
    resource function get authors() returns Author[];
    remote function addBook(string title, int authorId) returns Book?;
};

public distinct service class Author {
    resource function get id() returns @graphql:ID string {
    }

    resource function get name() returns string {
    }
}

public distinct service class Book {
    resource function get id() returns int {
    }

    resource function get title() returns string {
    }

    resource function get price() returns float {
    }
}
