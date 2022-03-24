
isolated function performDataBinding(json graphqlResponse, typedesc<graphql:DataResponse> targetType)
                                    returns graphql:DataResponse|graphql:ClientError {
    do {
        map<json> responseMap = <map<json>>graphqlResponse;
        json responseData = responseMap.get("data");
        if (responseMap.hasKey("extensions")) {
            responseData = check responseData.mergeJson({"__extensions": responseMap.get("extensions")});
        }
        graphql:DataResponse response = check responseData.cloneWithType(targetType);
        return response;
    } on fail var e {
        return error graphql:ClientError("GraphQL Client Error", e);
    }
}
