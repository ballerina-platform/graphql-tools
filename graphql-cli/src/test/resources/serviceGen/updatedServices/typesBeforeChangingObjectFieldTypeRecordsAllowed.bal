import ballerina/graphql;

type SchemaWithChangedObjectFieldTypeApi service object {
    *graphql:Service;
    resource function get book(int id, string? title) returns Book?;
    resource function get books() returns Book[]?;
};

public type Book record {|
    int id;
    string title;
|};
