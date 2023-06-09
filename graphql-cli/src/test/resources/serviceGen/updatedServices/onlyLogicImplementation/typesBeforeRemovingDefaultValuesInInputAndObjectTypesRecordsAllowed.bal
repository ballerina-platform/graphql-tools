import ballerina/graphql;

type SchemaWithRemovedDefaultValuesInInputAndObjectTypesApi service object {
    *graphql:Service;
    resource function get book(int id, string? title) returns Book?;
    resource function get authors() returns Author[];
    remote function addBook(CreateBookInput input) returns Book?;
};

public type CreateBookInput record {|
    string title = "No title";
    float price = 100.0;
    string version = "v1.0";
|};

public type Author record {|
    int id = 1;
    string name = "No name";
|};

public type Book record {|
    int id = 10;
    string title = "No title";
    float price = 100.0;
|};
