import ballerina/graphql;

type SchemaWithMultipleInterfacesApiService service object {
    *graphql:Service;
    resource function get student(int id) returns Student?|error;
};

public type ContactInfo distinct service object {
    resource function get email() returns string|error;
};

public type ExamInfo distinct service object {
    resource function get pass(int marks) returns boolean|error;
};

public type Info distinct service object {
    resource function get name() returns string|error;
};

public distinct service class Student {
    *Info;
    *ExamInfo;

    resource function get id() returns int|error {
        return error("Not implemented");
    }

    resource function get name() returns string|error {
        return error("Not implemented");
    }

    resource function get pass(int marks) returns boolean|error {
        return error("Not implemented");
    }
}

public distinct service class Teacher {
    *Info;
    *ContactInfo;

    resource function get id() returns int|error {
        return error("Not implemented");
    }

    resource function get name() returns string|error {
        return error("Not implemented");
    }

    resource function get email() returns string|error {
        return error("Not implemented");
    }
}
