import ballerina/graphql;

type Schema09Api service object {
    *graphql:Service;

    resource function get studentInfo(int id) returns Info?;
};

type Info distinct service object {
    resource function get name() returns string;
};

service class Book {
    resource function get name() returns string {}
}

distinct service class Student {
    *Info;

    resource function get id() returns int {}
    resource function get name() returns string {}
}

distinct service class Teacher {
    *Info;

    resource function get id() returns int {}
    resource function get name() returns string {}
}
