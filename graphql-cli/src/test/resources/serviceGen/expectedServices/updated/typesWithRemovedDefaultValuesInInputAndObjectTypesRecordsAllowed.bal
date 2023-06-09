import ballerina/graphql;

type SchemaWithRemovedDefaultValuesInInputAndObjectTypesApi service object {
    *graphql:Service;
    resource function get book(int id, string? title) returns Book?;
    resource function get authors() returns Author[];
    remote function addBook(CreateBookInput input) returns Book?;
};

public type CreateBookInput record {|
    string title;
    float price;
    string version;
|};

public type Author record {|
    int id;
    string name;
|};

public type Book record {|
    int id;
    string title;
    float price;
|};
