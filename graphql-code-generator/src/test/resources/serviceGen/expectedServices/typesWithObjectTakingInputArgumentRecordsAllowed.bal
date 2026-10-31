import ballerina/graphql;

type SchemaWithObjectTakingInputArgumentApiService service object {
    *graphql:Service;
    resource function get dog(string name) returns Dog?|error;
    resource function get cat(string name) returns Cat?|error;
};

public type Cat record {|
    string name;
    int age;
|};

public distinct service class Dog {
    resource function get name() returns string|error {
        return error("Not implemented");
    }

    resource function get knowsWord(string word) returns boolean|error {
        return error("Not implemented");
    }
}
