import ballerina/graphql;

configurable int port = 9090;

service SchemaWithBasic03Api on new graphql:Listener(port) {
}
