import ballerina/graphql;

type SchemaWithDefaultParameters03ApiService service object {
    *graphql:Service;
    resource function get book(int id) returns Book?|error;
    resource function get books(int[]? ids = [1, 2]) returns Book[]?|error;
    resource function get booksOfBooks(int[][]? idOfIds = [[1]]) returns Book[][]?|error;
    remote function addBook(string title, int authorId) returns Book?|error;
    resource function subscribe bookTitles() returns stream<string>|error;
};

public distinct service class Book {
    resource function get id() returns int|error {
        return error("Not implemented");
    }

    resource function get title() returns string|error {
        return error("Not implemented");
    }

    resource function get price() returns float|error {
        return error("Not implemented");
    }
}
