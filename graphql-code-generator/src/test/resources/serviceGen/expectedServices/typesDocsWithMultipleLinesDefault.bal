import ballerina/graphql;

type SchemaDocsWithMultipleLinesApiService service object {
    *graphql:Service;
    # Fetch all the books from database
    resource function get books() returns Book?[]?|error;
    # Fetch a book
    # by its id
    # + id - The id of the book to fetch
    # id is non nullable
    # + title - The title of the book to fetch
    # title is non nullable
    resource function get book(int id, string title) returns Book?|error;
};

public distinct service class Book {
    resource function get id() returns int|error {
        return error("Not implemented");
    }

    resource function get title() returns string|error {
        return error("Not implemented");
    }
}
