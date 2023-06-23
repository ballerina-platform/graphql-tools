import ballerina/graphql;

type SchemaWithAddedNewUnionMembersApi service object {
    *graphql:Service;
    resource function get student(int id) returns Profile?;
    resource function get teacher(int id) returns Profile?;
};

public type Profile Student|Teacher|Clerk;

public distinct service class Clerk {
    resource function get id() returns int {
        return 100;
    }

    resource function get name() returns string {
        return "John";
    }
}

public distinct service class Student {
    resource function get id() returns int {
        return 1;
    }

    resource function get name() returns string {
        return "Silva";
    }
}

public distinct service class Teacher {
    resource function get id() returns int {
        return 10;
    }

    resource function get name() returns string {
        return "Sheldon";
    }
}
