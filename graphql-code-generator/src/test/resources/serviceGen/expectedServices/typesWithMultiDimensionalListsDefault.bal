import ballerina/graphql;

type SchemaWithMultiDimensionalListsApiService service object {
    *graphql:Service;
    resource function get book(int id, string? title) returns Book?|error;
    resource function get books() returns Book?[]?|error;
    resource function get booksOfBooksPattern1() returns Book?[]?[]?|error;
    resource function get booksOfBooksPattern2() returns Book[][]?|error;
    resource function get booksOfBooksPattern3() returns Book?[][]?|error;
    resource function get booksOfBooksPattern4() returns Book[]?[]?|error;
    resource function get booksOfBooksPattern5() returns Book?[]?[]|error;
    resource function get booksOfBooksPattern6() returns Book[][]|error;
    resource function get booksOfBooksPattern7() returns Book?[][]|error;
    resource function get booksOfBooksPattern8() returns Book[]?[]|error;
    resource function get booksOfBooksOfBooks() returns Book?[]?[]?[]?|error;
};

public distinct service class Book {
    resource function get id() returns int|error {
        return error("Not implemented");
    }

    resource function get title() returns string|error {
        return error("Not implemented");
    }
}
