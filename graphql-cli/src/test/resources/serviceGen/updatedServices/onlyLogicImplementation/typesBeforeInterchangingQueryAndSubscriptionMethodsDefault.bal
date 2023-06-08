import ballerina/graphql;

type SchemaWithInterchangedQueryAndSubscriptionMethodsApi service object {
    *graphql:Service;
    resource function get author() returns Author;
    resource function subscribe authorNames() returns stream<string>;
    resource function get book(int id) returns Book;
    resource function subscribe bookTitles() returns stream<string>;
};

public type Info distinct service object {
    resource function get names() returns string;
    resource function subscribe bookNames() returns stream<string>;
};

public distinct service class Author {
    *Info;
    resource function get id() returns int {
        return 1;
    }

    resource function get names() returns string {
        return "John";
    }

    resource function subscribe bookNames() returns stream<string> {
        return "Lord of the rings";
    }
}

public distinct service class Book {
    resource function get id() returns int {
        return 1;
    }

    resource function subscribe titles() returns stream<string> {
        return "Harry Potter";
    }

    resource function get price() returns float {
        return 100.0;
    }
}
