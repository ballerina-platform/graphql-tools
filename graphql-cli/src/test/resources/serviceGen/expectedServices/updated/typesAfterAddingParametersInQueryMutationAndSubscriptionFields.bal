import ballerina/graphql;

type SchemaWithAddedParametersInQueryMutationAndSubscriptionFieldsApi service object {
    *graphql:Service;
    resource function get book(int id, string? title) returns Book?;
    resource function get authors(int?[] ids = [1]) returns Author[];
    remote function addBook(string title, int authorId) returns Book?;
    remote function addAuthor(string name) returns Author?;
    resource function subscribe bookTitles(int?[] ids = []) returns stream<string>;
    resource function subscribe authorNames(int[] ids) returns stream<string>;
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
