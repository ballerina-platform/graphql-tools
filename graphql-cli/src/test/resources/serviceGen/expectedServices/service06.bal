import ballerina/graphql;

configurable int port = 9090;

service Schema06Api on new graphql:Listener(port) {
}