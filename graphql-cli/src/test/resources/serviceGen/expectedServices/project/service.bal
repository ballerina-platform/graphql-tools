import ballerina/graphql;

configurable int port = 9090;

service SchemaDocs11Api on new graphql:Listener(port) {
}
