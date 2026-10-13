import ballerina/graphql;

type SchemaWithDefaultParameters01Api service object {
    *graphql:Service;
    resource function get book(int id = 1, string? title = "no name", float? price = 0.0, boolean? available = false) returns Book?|error;
    resource function get books() returns Book?[]?|error;
    remote function addBook(string title, int authorId) returns Book?|error;
    resource function subscribe bookTitles() returns stream<string>|error;
};

public distinct service class Author {
    resource function get id() returns int|error {
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
