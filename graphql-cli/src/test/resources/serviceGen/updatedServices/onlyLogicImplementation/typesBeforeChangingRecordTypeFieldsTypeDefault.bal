import ballerina/graphql;

type SchemaWithChangedRecordTypeFieldsTypeApi service object {
    *graphql:Service;
    resource function get book(int id, string? title) returns Book?;
    resource function get books() returns Book?[]?;
    resource function get authors() returns Author[];
};

public type Author record {|
    string id;
    string name;
    string age;
    int noOfBooksWritten;
|};

public type Book record {|
    string id;
    string title;
    int price;
    float soldAmount;
|};
