import ballerina/graphql;

type SchemaWithAddedNewSubscriptionFieldsApi service object {
    *graphql:Service;
    resource function get book(int id, string? title) returns Book?;
    resource function get books() returns Book?[]?;
    resource function get authors() returns Author[];
    resource function subscribe bookTitles() returns stream<string>;
    resource function subscribe authorNames() returns stream<string>;
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
}