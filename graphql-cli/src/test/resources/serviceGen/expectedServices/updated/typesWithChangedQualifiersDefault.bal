import ballerina/graphql;

type SchemaWithChangedQualifiersApi service object {
    *graphql:Service;
    resource function get book(int id) returns Book?;
    resource function get author() returns Author;
    remote function addBook(string title) returns Book?;
    remote function addAuthor(string name) returns Author?;
    resource function subscribe bookTitles() returns stream<string>;
    resource function subscribe authorNames() returns stream<string>;
};

public type Info distinct service object {
    resource function get name() returns string;
};

public distinct service class Author {
    *Info;
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
