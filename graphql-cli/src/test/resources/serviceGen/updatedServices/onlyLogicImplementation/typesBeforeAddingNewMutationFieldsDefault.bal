import ballerina/graphql;

type SchemaWithAddedNewMutationFieldsApi service object {
    *graphql:Service;
    resource function get book(int id, string? title) returns Book?;
    resource function get books() returns Book?[]?;
    resource function get authors() returns Author[];
    remote function addBook(string title, int authorId) returns Book?;
};

public distinct service class Author {
    resource function get id() returns string {
        return "1";
    }

    resource function get name() returns string {
        return "Dave";
    }
}

public distinct service class Book {
    resource function get id() returns int {
        return 1;
    }

    resource function get title() returns string {
        return "GraphQL for beginners";
    }

    resource function get price() returns float {
        return 100.0;
    }
}
