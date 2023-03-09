import ballerina/graphql;

type Schema06Api service object {
    *graphql:Service;

    resource function get dog(string name) returns Dog?;
	resource function get cat(string name) returns Cat?;
};

type Cat record {
    string name;
    int age;
};

service class Dog {
    resource function get name() returns string {}
    resource function get knowsWord(string word) returns boolean {}
}
