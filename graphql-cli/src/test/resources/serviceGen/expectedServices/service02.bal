import ballerina/graphql;

configurable int port = 9090;

service Schema02Api on new graphql:Listener(port) {
}