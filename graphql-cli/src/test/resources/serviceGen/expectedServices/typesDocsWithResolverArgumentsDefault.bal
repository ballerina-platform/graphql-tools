import ballerina/graphql;

public type SchemaDocsWithResolverArgumentsApi service object {
    *graphql:Service;

    # Fetch all the books from database
	resource function get books() returns Book?[]?;
	# Fetch a book by its id
	# + id - The id of the book to fetch
    resource function get book(int id) returns Book?;
};

public distinct service class Book {
	resource function get id() returns int {}
	resource function get title() returns string {}
}
