import ballerina/graphql;

type SchemaWithAddedQueryFieldsIntoTypesWithReadonlyObjectAndClassApi service object {
    *graphql:Service;
    resource function get book(int id, string? title) returns Book?;
};

public type Info readonly & isolated service object {
    isolated resource function get name() returns string;
};

public distinct readonly service class Book {
    *Info;
    resource function get id() returns int {
        return 1;
    }

    isolated resource function get name() returns string {
        return "Harry Potter";
    }
}
