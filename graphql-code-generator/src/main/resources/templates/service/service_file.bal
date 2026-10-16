import ballerina/graphql;

configurable int port = 9090;

service {{name}} on new graphql:Listener(port) {
{{resolvers}}
}
