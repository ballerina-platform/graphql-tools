import ballerina/graphql;

public type SchemaDocsWithMutationAndSubscriptionResolversApi service object {
    *graphql:Service;

	# Fetch a book by its id
    resource function get book(int id) returns Book?;
    # Fetch all the books
	resource function get books() returns Book?[]?;
	# Create a new book
	remote function createBook(string title) returns Book?;
	# Create a new author
	remote function createAuthor(string name) returns Author?;
	# Get stream of book titles
	resource function subscribe bookTitles() returns stream<string>;
};

public distinct service class Author {
	resource function get id() returns int {}
	resource function get name() returns string {}
}

public distinct service class Book {
	resource function get id() returns int {}
	resource function get title() returns string {}
}
