import ballerina/graphql;

type SchemaWithRemovedParameterDefaultValuesApi service object {
    *graphql:Service;
    resource function get book(int id = 1, string? title = "No title") returns Book?;
    resource function get authors(int?[] ids = [1]) returns Author[];
    remote function addBook(string title, int authorId = 1) returns Book?;
    remote function addBookMethod2(CreateBookInput input) returns Book;
    remote function addAuthor(string name = "No name") returns Author?;
    resource function subscribe bookTitles(int?[] ids = []) returns stream<string>;
    resource function subscribe authorNames(int[] ids) returns stream<string>;
};

public type CreateBookInput record {|
    string title;
    float price = 150.0;
    string version = "v1.0";
|};

public distinct service class Author {
    resource function get id() returns int {
        return 1;
    }

    resource function get name(string designation = "") returns string {
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

    resource function get price(int copiesSold = 0) returns float {
        return 100.0;
    }
}
