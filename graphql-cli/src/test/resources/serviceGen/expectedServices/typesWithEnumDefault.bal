import ballerina/graphql;

type SchemaWithEnumApi service object {
    *graphql:Service;
    resource function get student(int id) returns Student?;
};

public enum Gender {
    MALE,
    FEMALE
}

public enum Status {
    ONLINE,
    OFFLINE
}

public distinct service class Student {
    resource function get id() returns int {
    }

    resource function get name() returns string {
    }

    resource function get gender() returns Gender {
    }

    resource function get status() returns Status? {
    }
}
