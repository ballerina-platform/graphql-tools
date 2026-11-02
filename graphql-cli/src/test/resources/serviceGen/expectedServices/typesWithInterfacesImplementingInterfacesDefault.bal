import ballerina/graphql;

type SchemaWithInterfacesImplementingInterfacesApiService service object {
    *graphql:Service;
    resource function get student(int id) returns Student?|error;
};

public type AddressInfo distinct service object {
    *ContactInfo;
    resource function get address() returns string|error;
};

public type ContactInfo distinct service object {
    *Info;
    resource function get email() returns string|error;
};

public type Info distinct service object {
    resource function get name() returns string|error;
};

public distinct service class Student {
    *Info;

    resource function get id() returns int|error {
        return error("Not implemented");
    }

    resource function get name() returns string|error {
        return error("Not implemented");
    }
}

public distinct service class Teacher {
    *AddressInfo;

    resource function get id() returns int|error {
        return error("Not implemented");
    }

    resource function get name() returns string|error {
        return error("Not implemented");
    }

    resource function get email() returns string|error {
        return error("Not implemented");
    }

    resource function get address() returns string|error {
        return error("Not implemented");
    }
}
