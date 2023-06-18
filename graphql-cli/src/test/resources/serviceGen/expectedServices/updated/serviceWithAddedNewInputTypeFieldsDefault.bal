import ballerina/graphql;

configurable int port = 9090;

service SchemaWithAddedNewInputTypeFieldsApi on new graphql:Listener(port) {
    resource function get book(int id, string? title) returns Book? {
        return null;
    }

    resource function get books() returns Book?[]? {
        return null;
    }

    resource function get authors() returns Author[] {
        Author[] authors = [new Author()];
        return authors;
    }

    remote function createBook(CreateBookInput input) returns Book {
        return new Book();
    }

    remote function createAuthor(CreateAuthorInput? input) returns Author? {
        return null;
    }
}
