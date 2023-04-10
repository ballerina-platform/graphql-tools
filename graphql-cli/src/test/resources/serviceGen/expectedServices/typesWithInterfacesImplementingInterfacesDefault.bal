import ballerina/graphql;

public type SchemaWithInterfacesImplementingInterfacesApi service object {
    *graphql:Service;

    resource function get student(int id) returns Student?;
};

public type AddressInfo distinct service object {
    *ContactInfo;

    resource function get address() returns string;
};

public type ContactInfo distinct service object {
    *Info;

    resource function get email() returns string;
};

public type Info distinct service object {
    resource function get name() returns string;
};

public distinct service class Student {
    *Info;

    resource function get id() returns int {}
    resource function get name() returns string {}
}

public distinct service class Teacher {
    *AddressInfo;

    resource function get id() returns int {}
    resource function get name() returns string {}
    resource function get email() returns string {}
    resource function get address() returns string {}
}
