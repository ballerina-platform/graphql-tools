import ballerina/graphql;

table<Profile> key(id) profiles = table [
        {id: 1, name: "Walter White", age: 50},
        {id: 2, name: "Jesse Pinkman", age: 25}
    ];

configurable int port = 9090;

service SchemaWithAddedNewQueryFieldsApi on new graphql:Listener(port) {
    resource function get book(int id, string? title) returns Book? {
        return new Book();
    }
}
