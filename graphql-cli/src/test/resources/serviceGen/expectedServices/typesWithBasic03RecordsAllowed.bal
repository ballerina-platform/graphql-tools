import ballerina/graphql;

type SchemaWithBasic03Api service object {
    *graphql:Service;
    resource function get dog(string name) returns Dog?;
    resource function get cat(string name) returns Cat?;
};

public type Cat record {|
    string name;
    int age;
|};

public distinct service class Dog {
    resource function get name() returns string {
    }

    resource function get knowsWord(string word) returns boolean {
    }
}
