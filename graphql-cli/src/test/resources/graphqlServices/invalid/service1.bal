import ballerina/graphql;

service /hello on new graphql:Listener(9000) {
    resource function name() returns string {
        return "Ballerina GraphQL";
    }
}
