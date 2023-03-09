import ballerina/graphql;

type Schema10Api service object {
    *graphql:Service;

    resource function get student(int id) returns Student?;
};

type ContactInfo distinct service object {
    resource function get email() returns string;
};

type Info distinct service object {
    resource function get name() returns string;
};

distinct service class Student {
    *Info;

    resource function get id() returns int {}
    resource function get name() returns string {}
}

distinct service class Teacher {
    *Info;
    *ContactInfo;

    resource function get id() returns int {}
    resource function get name() returns string {}
    resource function get email() returns string {}
}