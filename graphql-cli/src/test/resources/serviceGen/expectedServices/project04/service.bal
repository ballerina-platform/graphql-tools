import ballerina/graphql;

configurable int port = 9090;

service Schema04Api on new graphql:Listener(port) {
}
