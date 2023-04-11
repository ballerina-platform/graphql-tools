import ballerina/graphql;

public type SchemaWithMultipleInterfacesApi service object {
    *graphql:Service;

    resource function get student(int id) returns Student?;
};

public type ContactInfo distinct service object {
    resource function get email() returns string;
};

public type ExamInfo distinct service object {
    resource function get pass(int marks) returns boolean;
};

public type Info distinct service object {
    resource function get name() returns string;
};

public distinct service class Student {
    *Info;
    *ExamInfo;

    resource function get id() returns int {}
    resource function get name() returns string {}
    resource function get pass(int marks) returns boolean {}
}

public distinct service class Teacher {
    *Info;
    *ContactInfo;

    resource function get id() returns int {}
    resource function get name() returns string {}
    resource function get email() returns string {}
}
