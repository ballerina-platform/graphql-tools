import ballerina/graphql;

type SchemaWithRemovedFieldsInQueryMutationAndSubscriptionApi service object {
    *graphql:Service;
    resource function get book(int id, string? title) returns Book?;
    resource function get books() returns Book?[]?;
    resource function get authors() returns Author[];
    remote function addBook(string title, int authorId) returns Book?;
    remote function updateBook(string title) returns Book?;
    remote function addAuthor(string name) returns Author?;
    resource function subscribe bookTitles() returns stream<string>;
    resource function subscribe authorNames() returns stream<string>;
    resource function subscribe bookIds() returns stream<int>;
    resource function subscribe authorIds() returns stream<int>;
};

public distinct service class Author {
    resource function get id() returns int {
        return 1;
    }

    resource function get name() returns string {
        return "John";
    }
}

public distinct service class Book {
    resource function get id() returns int {
        return 1;
    }

    resource function get title() returns string {
        return "Harry Potter";
    }

    resource function get price() returns float {
        return 100.0;
    }
}
