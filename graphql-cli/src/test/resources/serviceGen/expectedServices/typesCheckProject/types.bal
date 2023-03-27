import ballerina/graphql;

public type Schema08Api service object {
    *graphql:Service;
    resource function get student(int id) returns Student?;
    resource function get teacher(int id) returns Teacher?;
};

public type Profile Student|Teacher|Clerk;

public service class Clerk {
    resource function get id() returns int {
    }
    resource function get name() returns string {
    }
}

public service class Student {
    resource function get id() returns int {
    }
    resource function get name() returns string {
    }
}

public service class Teacher {
    resource function get id() returns int {
    }
    resource function get name() returns string {
    }
}
