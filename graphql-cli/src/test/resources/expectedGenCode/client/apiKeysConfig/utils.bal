type SimpleBasicType string|boolean|int|float|decimal;

# Generate header map for given header values.
#
# + headerParam - Headers  map
# + return - Returns generated map or error at failure of client initialization
isolated function getMapForHeaders(map<any> headerParam) returns map<string|string[]> {
    map<string|string[]> headerMap = {};
    foreach var [key, value] in headerParam.entries() {
        if value is string || value is string[] {
            headerMap[key] = value;
        } else if value is int[] {
            string[] stringArray = [];
            foreach int intValue in value {
                stringArray.push(intValue.toString());
            }
            headerMap[key] = stringArray;
        } else if value is SimpleBasicType {
            headerMap[key] = value.toString();
        }
    }
    return headerMap;
}

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
