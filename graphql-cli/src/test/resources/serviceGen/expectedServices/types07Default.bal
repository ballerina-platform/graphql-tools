import ballerina/graphql;

type Schema07Api service object {
    *graphql:Service;

    resource function get student(int id) returns Student?;
};

service class Student {
    resource function get id() returns int {}
    resource function get name() returns string {}
}

enum Gender {
    MALE,
    FEMALE
}