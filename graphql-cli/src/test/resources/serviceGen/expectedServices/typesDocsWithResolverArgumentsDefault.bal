import ballerina/graphql;

type SchemaDocsWithResolverArgumentsApiService service object {
    *graphql:Service;
    # Fetch all the books from database
    resource function get books() returns Book?[]?|error;
    # Fetch a book by its id
    # + id - The id of the book to fetch
    resource function get book(int id) returns Book?|error;
};

public distinct service class Book {
    resource function get id() returns int|error {
        return error("Not implemented");
    }

    resource function get title() returns string|error {
        return error("Not implemented");
    }
}
