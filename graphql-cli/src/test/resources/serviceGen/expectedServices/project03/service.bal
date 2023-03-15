import ballerina/graphql;

configurable int port = 9090;

service Schema03Api on new graphql:Listener(port) {
}
