import ballerina/graphql;

type SchemaWithAddedNewInterfaceApi service object {
    *graphql:Service;
    resource function get studentInfo(int id) returns Info?;
};

public type Info distinct service object {
    resource function get name() returns string;
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
}

public distinct service class Teacher {
    *Info;
    resource function get id() returns int {
        return 10;
    }

    resource function get name() returns string {
        return "Michelle";
    }
}

public type ContactInfo distinct service object {
    resource function get phone() returns string;
};
