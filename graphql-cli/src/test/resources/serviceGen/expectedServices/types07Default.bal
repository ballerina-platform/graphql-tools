import ballerina/graphql;

type Schema07Api service object {
    *graphql:Service;

    resource function get student(int id) returns Student?;
};

enum Gender {
    MALE,
    FEMALE
}

service class Student {
    resource function get id() returns int {}
    resource function get name() returns string {}
}