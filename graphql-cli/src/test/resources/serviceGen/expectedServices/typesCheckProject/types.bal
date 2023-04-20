import ballerina/graphql;

type SchemaWithUnionApi service object {
    *graphql:Service;
    resource function get student(int id) returns Profile?;
    resource function get teacher(int id) returns Profile?;
};

public type Profile Student|Teacher|Clerk;

public distinct service class Clerk {
    resource function get id() returns int {
    }

    resource function get name() returns string {
    }
}

public distinct service class Student {
    resource function get id() returns int {
    }

    resource function get name() returns string {
    }
}

public distinct service class Teacher {
    resource function get id() returns int {
    }

    resource function get name() returns string {
    }
}
