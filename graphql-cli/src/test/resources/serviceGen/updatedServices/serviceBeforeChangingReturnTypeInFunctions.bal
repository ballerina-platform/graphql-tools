import ballerina/graphql;

configurable int port = 9090;

service SchemaWithChangedReturnTypeInFunctionsApi on new graphql:Listener(port) {
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

    remote function createBook(string title) returns Book? {
        return new Book();
    }

    remote function createAuthor(String name) returns Author {
        return new Author();
    }

    resource function subscribe bookTitles() returns stream<string?> {
        return ["Harry Potter", "The Alchemist"].toStream();
    }

    resource function subscribe authorNames() returns stream<string> {
        return ["Emma", "Watson"].toStream();
    }
}
