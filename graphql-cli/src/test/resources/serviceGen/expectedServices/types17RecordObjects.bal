import ballerina/graphql;

type Schema17Api service object {
    *graphql:Service;

    resource function get book(int id) returns Book?;
};

type Book record {
    int id;
    string title;
    float price?;
};



