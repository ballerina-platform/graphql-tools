import ballerina/graphql;

configurable int port = 9090;

service SchemaWithObjectTakingInputArgumentApiService on new graphql:Listener(port) {
    resource function get dog(string name) returns Dog?|error {
        return error("Not implemented");
    }

    resource function get cat(string name) returns Cat?|error {
        return error("Not implemented");
    }
}
