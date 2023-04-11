import ballerina/graphql;

configurable int port = 9090;

service SchemaWithBasic01Api on new graphql:Listener(port) {
}
