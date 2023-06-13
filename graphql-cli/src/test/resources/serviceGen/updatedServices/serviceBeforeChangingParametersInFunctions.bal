import ballerina/graphql;

configurable int port = 9090;

service SchemaWithChangedParametersInFunctionsApi on new graphql:Listener(port) {
    resource function get book(int? id) returns Book? {
        return new Book();
    }

    resource function get books(int[] ids) returns Book?[]? {
        return null;
    }

    resource function get authors(int[] ids) returns Author[] {
        Author[] authors = [new Author()];
        return authors;
    }

    remote function addBook(int authorId, string title = "No title", int price = 10) returns Book? {
        return null;
    }
}
