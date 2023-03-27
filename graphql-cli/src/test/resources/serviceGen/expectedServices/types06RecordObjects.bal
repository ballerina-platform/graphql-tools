import ballerina/graphql;

public type Schema06Api service object {
    *graphql:Service;

    resource function get dog(string name) returns Dog?;
	resource function get cat(string name) returns Cat?;
};

public type Cat record {
    string name;
    int age;
};

public service class Dog {
    resource function get name() returns string {}
    resource function get knowsWord(string word) returns boolean {}
}
