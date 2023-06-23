import ballerina/graphql;

configurable int port = 9090;

service SchemaWithChangedParametersInFunctionsApi on new graphql:Listener(port) {
    resource function get book(int id, string? title) returns Book? {
        return new Book();
    }

    resource function get books() returns Book?[]? {
        return null;
    }

    resource function get authors(string[] ids) returns Author[] {
        Author[] authors = [new Author()];
        return authors;
    }

    remote function addBook(string title, int authorId = 1, float price = 10.0) returns Book? {
        return null;
    }
}
