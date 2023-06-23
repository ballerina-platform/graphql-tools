import ballerina/graphql;

type SchemaWithChangedQualifiersApi service object {
    *graphql:Service;
    remote function book(int id) returns Book?;
    remote function author() returns Author;
    resource function get addBook(string title) returns Book?;
    resource function get addAuthor(string name) returns Author?;
    resource function subscribe bookTitles() returns stream<string>;
    remote function authorNames() returns stream<string>;
};

public type Info distinct service object {
    remote function name() returns string;
};

public distinct service class Author {
    *Info;
    resource function get id() returns int {
        return 1;
    }

    remote function name() returns string {
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

    remote function price() returns float {
        return 100.0;
    }
}
