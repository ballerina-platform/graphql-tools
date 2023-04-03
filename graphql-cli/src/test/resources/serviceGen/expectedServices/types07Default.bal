import ballerina/graphql;

public type Schema07Api service object {
    *graphql:Service;

    resource function get student(int id) returns Student?;
};

public enum Gender {
    MALE,
    FEMALE
}

public distinct service class Student {
    resource function get id() returns int {}
    resource function get name() returns string {}
}