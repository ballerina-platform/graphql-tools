import ballerina/graphql;

type SchemaWithChangedDefaultValuesApi service object {
    *graphql:Service;
    resource function get book(int id, string? title = "No title") returns Book?;
    resource function get authors() returns Author[];
    remote function addBook(CreateBookInput input) returns Book?;
};

public type CreateBookInput record {|
    string title = "No title";
    float price = 10.0;
|};

public distinct service class Author {
    resource function get id() returns int {
        return 100;
    }

    resource function get name() returns string {
        return "John";
    }
}

public distinct service class Book {
    resource function get id() returns int {
        return 10;
    }

    resource function get title() returns string {
        return "John";
    }

    resource function get price(int pageCount = 10) returns float {
        return 10.0;
    }
}
