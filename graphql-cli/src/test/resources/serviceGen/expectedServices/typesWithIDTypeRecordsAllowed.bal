import ballerina/graphql;

type SchemaWithIDTypeApi service object {
    *graphql:Service;
    # Fetch a book by its id
    # + id - The id of the book to fetch
    resource function get book(@graphql:ID string id) returns Book?;
    # Fetch a list of books by their ids
    # + ids - The list of book ids to fetch
    resource function get books(@graphql:ID string[] ids) returns Book[];
};

# Represents a book written by an author
public type Book record {|
    # The id of the book, unique identifier
    @graphql:ID
    string id;
    # The title of the book
    string title;
|};
