import ballerina/graphql;

type SchemaWithInterfaceApi service object {
    *graphql:Service;
    resource function get studentInfo(int id) returns Info?|error;
    resource function get teacherInfo(int id) returns Info|error;
    resource function get book(string name) returns Book|error;
};

public distinct service class Book {
    resource function get name() returns string|error {
        return error("Not implemented");
    }
}

public type Info distinct service object {
    resource function get name() returns string|error;
};

public distinct service class Student {
    *Info;

    resource function get id() returns int|error {
        return error("Not implemented");
    }

    resource function get name() returns string|error {
        return error("Not implemented");
    }
}

public distinct service class Teacher {
    *Info;

    resource function get id() returns int|error {
        return error("Not implemented");
    }

    resource function get name() returns string|error {
        return error("Not implemented");
    }
}
