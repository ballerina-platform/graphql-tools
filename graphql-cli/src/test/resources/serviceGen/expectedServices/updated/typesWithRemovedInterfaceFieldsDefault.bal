import ballerina/graphql;

type SchemaWithRemovedInterfaceFieldsApi service object {
    *graphql:Service;
    resource function get studentInfo(int id) returns Info?;
    resource function get book(string name) returns Book;
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

    resource function get phone() returns string {
        return "0741545723";
    }

    resource function get email() returns string {
        return "john@gmail.com";
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

    resource function get phone() returns string {
        return "0741545123";
    }

    resource function get email() returns string {
        return "michelle@gmail.com";
    }
}
