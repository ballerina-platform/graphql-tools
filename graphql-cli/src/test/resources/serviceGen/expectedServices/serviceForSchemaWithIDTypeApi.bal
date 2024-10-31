import ballerina/graphql;

configurable int port = 9090;

service SchemaWithIDTypeApi on new graphql:Listener(port) {
    # Fetch a book by its id
    # + id - The id of the book to fetch
    resource function get book(@graphql:ID string id) returns Book? {
    }

    # Fetch a list of books by their ids
    # + ids - The list of book ids to fetch
    resource function get books(@graphql:ID string[] ids) returns Book[] {
    }
}
