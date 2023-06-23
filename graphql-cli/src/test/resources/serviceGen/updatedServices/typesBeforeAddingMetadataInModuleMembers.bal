import ballerina/graphql;

type SchemaWithMetadataInModuleMembersApi service object {
    *graphql:Service;
    resource function get book(int id) returns Book?;
    remote function addBook(CreateBookInput input) returns Book;
};

public type Info distinct service object {
    resource function get name() returns string;
};

public enum Available {
    YES,
    NO
}

public distinct service class Book {
    *Info;
    resource function get id() returns int {
    }

    resource function get name() returns string {
        return "Harry Potter";
    }
}

public distinct service class Novel {
    *Info;
    resource function get id() returns int {
    }

    resource function get name() returns string {
        return "Romeo and Juliet";
    }
}

public type ReadingMaterial Book|Novel;

public type CreateBookInput record {|
    string name;
|};
