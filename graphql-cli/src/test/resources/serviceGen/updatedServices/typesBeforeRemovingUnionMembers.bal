import ballerina/graphql;

type SchemaWithRemovedUnionMembersApi service object {
    *graphql:Service;
    resource function get student(int id) returns Profile?;
    resource function get parent(int id) returns Parent?;
    resource function get clerk(int id) returns Clerk?;
};

public type Profile Student|Teacher|Clerk|Parent;

public distinct service class Clerk {
    resource function get id() returns int {
        return 100;
    }

    resource function get name() returns string {
        return "John";
    }
}

public distinct service class Parent {
    resource function get id() returns int {
        return 1000;
    }

    resource function get name() returns string {
        return "Cooper";
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
