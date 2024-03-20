import ballerina/graphql;

type SchemaWithInterfaceApi service object {
    *graphql:Service;
    resource function get studentInfo(int id) returns Info?;
    resource function get teacherInfo(int id) returns Info;
    resource function get book(string name) returns Book;
};

public type Info distinct service object {
    resource function get name() returns string;
};

public type Book record {|
    string name;
|};

public distinct service class Student {
    *Info;
    resource function get id() returns int {
    }

    resource function get name() returns string {
    }
}

public distinct service class Teacher {
    *Info;
    resource function get id() returns int {
    }

    resource function get name() returns string {
    }
}
