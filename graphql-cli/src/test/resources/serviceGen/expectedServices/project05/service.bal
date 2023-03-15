import ballerina/graphql;

configurable int port = 9090;

service Schema05Api on new graphql:Listener(port) {
}
