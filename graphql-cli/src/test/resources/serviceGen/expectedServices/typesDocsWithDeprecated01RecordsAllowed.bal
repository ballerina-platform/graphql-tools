import ballerina/graphql;

public type SchemaDocsWithDeprecated01Api service object {
    *graphql:Service;

    # Fetch all the books from database
	resource function get books() returns Book?[]?;
	# Fetch a book by its id
	# + id - The id of the book to fetch
    resource function get book(int id) returns Book?;
};

# Represents a book written by an author
public type Book record {
    # The id of the book, unique identifier
    int id;
    # The title of the book
    # # Deprecated
    # Use `name` instead
    @deprecated
    string title;
    string name;
};
