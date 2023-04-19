import ballerina/jballerina.java;

// Prepare query string to resolve by reference.
isolated function wrapWithEntityRepresentation(string typename, map<json>[] fieldsRequiredToFetch, string fieldQuery) returns string {
    string[] representations = [];
    foreach var entry in fieldsRequiredToFetch {
        string keyValueString = getKeyValueString(entry);
        representations.push(string `{ __typename: "${typename}", ${keyValueString} }`);
    }
    return string `query{
        _entities(
            representations: [${string:'join(", ", ...representations)}]
        ) {
            ... on ${typename} {
                ${fieldQuery}
            }
        }
    }`;
}

// Prepare key value string. 
isolated function getKeyValueString(map<json> fieldMap) returns string {
    string keyValueString = "";
    foreach var [key, value] in fieldMap.entries() {
        if value is map<json> {
            keyValueString = keyValueString + string `${key}: { ${getKeyValueString(value)} } `;
        }
        else {
            keyValueString = keyValueString + string `${key}: "${value.toString()}" `;
        }
    }
    return keyValueString;
}

// Prepare query string to resolve by query.
public isolated function wrapwithQuery(string root, string fieldQuery, map<string>? args = ()) returns string {
    if args is () {
        return string `query
            {   
                ${root}{
                ${fieldQuery}
            }
        }`;
    }
    else {
        string[] argsList = [];
        foreach var [key, value] in args.entries() {
            argsList.push(string `${key}: ${value}`);
        }
        return string `query
            {
                ${root}(${string:'join(", ", ...argsList)}){
                ${fieldQuery}
            }
        }`;
    }
}

isolated function convertPathToStringArray((string|int)[] path) returns string[] {
    return path.'map(isolated function(string|int element) returns string {
        return element is int ? "@" : element;
    });
}

isolated function compose(map<json> initialResult, map<json> resultToCompose, string element) = @java:Method {
    'class: "io.ballerina.stdlib.NativeImpl"
} external;
