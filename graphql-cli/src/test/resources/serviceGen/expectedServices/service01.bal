import ballerina/graphql;

configurable int port = 9090;

service Schema01Api on new graphql:Listener(port) {
}