import ballerina/graphql;

type SchemaWithChangedRecordTypeFieldsTypeApi service object {
    *graphql:Service;
    resource function get book(int id, string? title) returns Book?;
    resource function get books() returns Book?[]?;
    resource function get authors() returns Author[];
};

public type Author record {|
    int id;
    string name;
    int age;
    int noOfBooksWritten;
|};

public type Book record {|
    int id;
    string title;
    float price;
    int soldAmount;
|};
