import ballerina/graphql;

type SchemaWithAddedNewEnumFieldsApi service object {
    *graphql:Service;
    resource function get student(int id) returns Student?;
};

public enum Gender {
    MALE,
    FEMALE,
    OTHER
}

public distinct service class Student {
    resource function get id() returns int {
        return 1;
    }

    resource function get name() returns string {
        return "John";
    }
}
