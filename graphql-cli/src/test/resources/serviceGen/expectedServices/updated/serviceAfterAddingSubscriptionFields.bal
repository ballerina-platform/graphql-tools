import ballerina/graphql;

configurable int port = 9090;

service SchemaWithAddedNewSubscriptionFieldsApi on new graphql:Listener(port) {
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

    resource function subscribe bookTitles() returns stream<string> {
        return ["Harry Potter", "The Alchemist"].toStream();
    }

    resource function subscribe authorNames() returns stream<string> {
    }
}
