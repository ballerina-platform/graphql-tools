import ballerina/graphql;

configurable int port = 9090;

service SchemaWithChangedQualifiersInFunctionsApi on new graphql:Listener(port) {
    remote function books() returns Book?[]? {
        return null;
    }

    resource function get authors() returns Author[] {
        Author[] authors = [new Author()];
        return authors;
    }

    resource function get createBook(string title) returns Book {
        return new Book();
    }
}
