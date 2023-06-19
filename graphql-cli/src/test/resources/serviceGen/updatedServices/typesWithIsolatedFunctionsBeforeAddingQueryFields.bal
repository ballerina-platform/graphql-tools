import ballerina/graphql;

type SchemaWithAddedQueryFieldsIntoTypesWithIsolatedFunctionsApi service object {
    *graphql:Service;
    isolated resource function get book(int id, string? title) returns Book?;
};

public type Info distinct service object {
    isolated resource function get name() returns string;
};

public distinct service class Book {
    *Info;
    resource function get id() returns int {
        return 1;
    }

    isolated resource function get name() returns string {
        return "Harry Potter";
    }
}
