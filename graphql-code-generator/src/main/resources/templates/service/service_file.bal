import ballerina/graphql;

configurable int port = 9090;

service on new graphql:Listener(port) {
{{resolvers}}
}
