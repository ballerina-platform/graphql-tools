import ballerina/graphql;

type SchemaWithEnumApiService service object {
    *graphql:Service;
    resource function get student(int id) returns Student?|error;
};

public distinct service class Student {
    resource function get id() returns int|error {
        return error("Not implemented");
    }

    resource function get name() returns string|error {
        return error("Not implemented");
    }

    resource function get gender() returns Gender|error {
        return error("Not implemented");
    }

    resource function get status() returns Status?|error {
        return error("Not implemented");
    }
}

public enum Gender {
    MALE,
    FEMALE
}

public enum Status {
    ONLINE,
    OFFLINE
}
