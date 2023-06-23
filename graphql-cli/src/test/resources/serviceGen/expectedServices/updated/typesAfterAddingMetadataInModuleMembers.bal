import ballerina/graphql;

type SchemaWithMetadataInModuleMembersApi service object {
    *graphql:Service;
    resource function get book(int id) returns Book?;
    remote function addBook(CreateBookInput input) returns Book;
};

# Create book input data
public type CreateBookInput record {|
    string name;
|};

# Interface to represent information of book
public type Info distinct service object {
    resource function get name() returns string;
};

# Availability of a book
public enum Available {
    YES,
    NO
}

# Represents any reading material
public type ReadingMaterial Book|Novel;

# Represents a book
public distinct service class Book {
    *Info;
    resource function get id() returns int {
    }

    resource function get name() returns string {
        return "Harry Potter";
    }

    resource function get isAvailable() returns Available {
    }
}

# Represents a novel
public distinct service class Novel {
    *Info;
    resource function get id() returns int {
    }

    resource function get name() returns string {
        return "Romeo and Juliet";
    }

    resource function get isAvailable() returns Available {
    }
}
