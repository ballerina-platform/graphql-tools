import ballerina/graphql;

type SchemaDocs05Api service object {
    *graphql:Service;

    # Fetch all the books from database
	resource function get books() returns Book?[]?;
	# Fetch a book
	# by its id
	# + id - The id of the book to fetch
	# id is non nullable
	# + title - The title of the book to fetch
	# title is non nullable
    resource function get book(int id, string title) returns Book?;
};

service class Book {
	resource function get id() returns int {}
	resource function get title() returns string {}
}
