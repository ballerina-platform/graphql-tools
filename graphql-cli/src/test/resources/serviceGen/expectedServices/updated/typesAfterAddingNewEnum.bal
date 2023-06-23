import ballerina/graphql;

type SchemaWithAddedNewEnumApi service object {
    *graphql:Service;
    resource function get student(int id) returns Student?;
};

public enum Gender {
    MALE,
    FEMALE
}

public distinct service class Student {
    resource function get id() returns int {
        return 1;
    }

    resource function get name() returns string {
        return "John";
    }
}

public enum Category {
    JUNIOR,
    SENIOR
}

public distinct service class Author {
    resource function get id() returns int {
    }

    resource function get name() returns string {
    }
}
