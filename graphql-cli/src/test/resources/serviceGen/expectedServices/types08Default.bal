import ballerina/graphql;

public type Schema08Api service object {
    *graphql:Service;

    resource function get student(int id) returns Student?;
    resource function get teacher(int id) returns Teacher?;
};

public type Profile Student|Teacher|Clerk;

public distinct service class Clerk {
    resource function get id() returns int {}
    resource function get name() returns string {}
}

public distinct service class Student {
    resource function get id() returns int {}
    resource function get name() returns string {}
}

public distinct service class Teacher {
    resource function get id() returns int {}
    resource function get name() returns string {}
}