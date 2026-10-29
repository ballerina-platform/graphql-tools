import ballerina/graphql;

configurable int port = 9090;

service SchemaWithIDTypeApiService on new graphql:Listener(port) {
    # Fetch a book by its id
    # + id - The id of the book to fetch
    resource function get book(@graphql:ID string id) returns Book?|error {
        return error("Not implemented");
    }

    # Fetch a list of books by their ids
    # + ids - The list of book ids to fetch
    resource function get books(@graphql:ID string[] ids) returns Book[]|error {
        return error("Not implemented");
    }
}
