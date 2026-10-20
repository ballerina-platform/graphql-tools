import ballerina/graphql;

type SchemaWithUnionApi service object {
    *graphql:Service;
    resource function get student(int id) returns Profile?|error;
    resource function get teacher(int id) returns Profile?|error;
};

public distinct service class Clerk {
    resource function get id() returns int|error {
        return error("Not implemented");
    }

    resource function get name() returns string|error {
        return error("Not implemented");
    }
}

public type Profile Student|Teacher|Clerk;

public distinct service class Student {
    resource function get id() returns int|error {
        return error("Not implemented");
    }

    resource function get name() returns string|error {
        return error("Not implemented");
    }
}

public distinct service class Teacher {
    resource function get id() returns int|error {
        return error("Not implemented");
    }

    resource function get name() returns string|error {
        return error("Not implemented");
    }
}
