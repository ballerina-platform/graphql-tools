import ballerina/graphql;

type SchemaWithAddedNewInterfaceTypeFieldsApi service object {
    *graphql:Service;
    resource function get studentInfo(int id) returns Info?;
};

public type Info distinct service object {
    resource function get name() returns string;
    resource function get age() returns int;
};

public distinct service class Book {
    resource function get name() returns string {
        return "Harry Potter";
    }
}

public distinct service class Student {
    *Info;
    resource function get id() returns int {
        return 1;
    }

    resource function get name() returns string {
        return "John";
    }

    resource function get age() returns int {
    }
}

public distinct service class Teacher {
    *Info;
    resource function get id() returns int {
        return 10;
    }

    resource function get name() returns string {
        return "Michelle";
    }

    resource function get age() returns int {
    }
}
