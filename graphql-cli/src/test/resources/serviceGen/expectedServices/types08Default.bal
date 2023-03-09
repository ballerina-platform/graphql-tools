import ballerina/graphql;

type Schema08Api service object {
    *graphql:Service;

    resource function get student(int id) returns Student?;
    resource function get teacher(int id) returns Teacher?;
};

service class Clerk {
    resource function get id() returns int {}
    resource function get name() returns string {}
}

service class Student {
    resource function get id() returns int {}
    resource function get name() returns string {}
}

service class Teacher {
    resource function get id() returns int {}
    resource function get name() returns string {}
}

type Profile Student|Teacher|Clerk;