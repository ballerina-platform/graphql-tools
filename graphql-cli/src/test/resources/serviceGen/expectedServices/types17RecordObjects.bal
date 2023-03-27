import ballerina/graphql;

public type Schema17Api service object {
    *graphql:Service;

    resource function get book(int id) returns Book?;
};

public type Book record {
    int id;
    string title;
    float price?;
};
