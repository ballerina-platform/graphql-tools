import ballerina/graphql;

type Schema02Api service object {
    *graphql:Service;
    resource function get book(int id, string? title) returns Book?;
    resource function get books() returns Book?[]?;
    resource function get authors() returns Author[];
};

service class Author {
    resource function get id() returns int {
    }
    resource function get name() returns string {
    }
}

service class Book {
    resource function get id() returns int {
    }
    resource function get title() returns string {
    }
}
