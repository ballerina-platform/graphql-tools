import ballerina/graphql;

configurable int port = 9090;

service SchemaWithAddedNewMutationFieldsApi on new graphql:Listener(port) {
    resource function get book(int id, string? title) returns Book? {
        return new Book();
    }

    resource function get books() returns Book?[]? {
        return null;
    }

    resource function get authors() returns Author[] {
        Author[] authors = [new Author()];
        return authors;
    }

    remote function addBook(string title, int authorId) returns Book? {
        return null;
    }

    remote function addAuthor(string name) returns Author? {
    }
}
