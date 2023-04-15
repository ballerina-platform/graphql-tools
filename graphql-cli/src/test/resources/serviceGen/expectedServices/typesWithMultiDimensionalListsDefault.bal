import ballerina/graphql;

type SchemaWithMultiDimensionalListsApi service object {
    *graphql:Service;

    resource function get book(int id, string? title) returns Book?;
	resource function get books() returns Book?[]?;
	resource function get booksOfBooksPattern1() returns Book?[]?[]?;
	resource function get booksOfBooksPattern2() returns Book[][]?;
	resource function get booksOfBooksPattern3() returns Book?[][]?;
	resource function get booksOfBooksPattern4() returns Book[]?[]?;
	resource function get booksOfBooksPattern5() returns Book?[]?[];
    resource function get booksOfBooksPattern6() returns Book[][];
    resource function get booksOfBooksPattern7() returns Book?[][];
    resource function get booksOfBooksPattern8() returns Book[]?[];
	resource function get booksOfBooksOfBooks() returns Book?[]?[]?[]?;
};

public distinct service class Book {
	resource function get id() returns int {}
	resource function get title() returns string {}
}
