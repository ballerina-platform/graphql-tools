import ballerina/graphql;

type SchemaWithDefaultParameters02ApiService service object {
    *graphql:Service;
    resource function get book(int id, Availability? available = UNAVAILABLE) returns Book?|error;
    remote function addBook(string title, int authorId) returns Book?|error;
    resource function subscribe bookTitles() returns stream<string>|error;
};

public distinct service class Book {
    resource function get id() returns int|error {
        return error("Not implemented");
    }

    resource function get title() returns string|error {
        return error("Not implemented");
    }

    resource function get price() returns float|error {
        return error("Not implemented");
    }
}

public enum Availability {
    AVAILABLE,
    UNAVAILABLE
}
