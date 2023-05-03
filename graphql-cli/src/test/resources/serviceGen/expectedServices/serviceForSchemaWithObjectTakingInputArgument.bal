import ballerina/graphql;

configurable int port = 9090;

service SchemaWithObjectTakingInputArgumentApi on new graphql:Listener(port) {
    resource function get dog(string name) returns Dog? {
    }

    resource function get cat(string name) returns Cat? {
    }
}
