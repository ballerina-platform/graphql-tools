import ballerina/graphql;

type SchemaWithMutationApiService service object {
    *graphql:Service;
    resource function get book(int id, string? title) returns Book?|error;
    resource function get books() returns Book?[]?|error;
    resource function get authors() returns Author[]|error;
    remote function addBook(string title, int authorId) returns Book?|error;
};

public distinct service class Author {
    resource function get id() returns @graphql:ID string|error {
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

    resource function get price() returns float|error {
        return error("Not implemented");
    }
}
