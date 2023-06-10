import ballerina/graphql;

type SchemaWithAddedNewObjectTypeFieldsInMultipleObjectsApi service object {
    *graphql:Service;
    resource function get book(int id, string? title) returns Book?;
    resource function get books() returns Book?[]?;
    resource function get authors() returns Author[];
};

public distinct service class Author {
    resource function get id() returns int {
        return 1;
    }

    resource function get name() returns string {
        return "John";
    }

    resource function get age() returns int {
    }
}

public distinct service class Book {
    resource function get id() returns int {
        return 10;
    }

    resource function get title() returns string {
        return "Harry Potter";
    }

    resource function get price() returns float {
    }
}
