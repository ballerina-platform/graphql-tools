import ballerina/lang.value;
import ballerina/graphql;

public class Resolver {

    private UnResolvableField[] toBeResolved;

    // The final result of the resolver. Created an composed while resolving by `resolve()`.
    private json result;
    private string resultType;
    private string[] currentPath;

    // Query plan used to classify the fields.
    private final readonly & table<QueryPlanEntry> key(typename) queryPlan;

    public isolated function init(readonly & table<QueryPlanEntry> key(typename) queryPlan, json result, string resultType, UnResolvableField[] unResolvableFields, string[] currentPath) {
        self.queryPlan = queryPlan;
        self.result = result;
        self.resultType = resultType;
        self.toBeResolved = unResolvableFields;
        self.currentPath = currentPath; // Path upto the result fields.
    }

    public isolated function getResult() returns json|error {
        if self.toBeResolved.length() > 0 {
            check self.resolve();
        }
        return self.result;
    }

    isolated function resolve() returns error? {
        // Resolve the fields which are not resolved yet.
        while self.toBeResolved.length() > 0 {
            UnResolvableField 'record = self.toBeResolved.shift();
            string[] path = self.getEffectivePath('record.'field);

            // Check whether the field need to be resolved is nested by zero or one level.
            // These can be resolved and composed directly to the result.
            if path.filter(e => e == "@").length() == 0 || (path.filter(e => e == "@").length() == 1 &&
            path.indexOf("@") == path.length() - 2) {
                string clientName = self.queryPlan.get('record.parent).fields.get('record.'field.getName()).'client;
                graphql:Client 'client = getClient(clientName);
                if path.indexOf("@") is () {
                    path = path.slice(0, path.length() - 1);
                } else {
                    path = path.slice(0, path.length() - 2);
                }

                RequiresFieldRecord[]? requiredFields = self.queryPlan.get('record.parent).fields.get('record.'field.getName()).requires;
                map<json>[] requiredFieldWithValues = check self.getRequiredFieldsInPath(self.result, self.resultType, clientName, path, requiredFields);

                if 'record.'field.getUnwrappedType().kind == "SCALAR" {
                    // If the field type is a scalar type, just pass the field name wrapped with entity representation.
                    string queryString = wrapWithEntityRepresentation('record.parent, requiredFieldWithValues, 'record.'field.getName());
                    EntityResponse result = check 'client->execute(queryString);
                    check self.compose(self.result, result.data._entities, self.getEffectivePath('record.'field));
                } else {
                    // Else need to classify the fields and resolve them accordingly.
                    QueryFieldClassifier classifier = new ('record.'field, self.queryPlan, clientName);
                    string fieldString = classifier.getFieldStringWithRoot();
                    string queryString = wrapWithEntityRepresentation('record.parent, requiredFieldWithValues, fieldString);
                    EntityResponse response = check 'client->execute(queryString);
                    check self.compose(self.result, response.data._entities, self.getEffectivePath('record.'field));
                    UnResolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();
                    if (propertiesNotResolved.length() > 0) {
                        Resolver resolver = new (self.queryPlan, self.result, self.resultType, propertiesNotResolved, self.currentPath);
                        check resolver.resolve();
                    }
                }

            } else {
                // Cannot resolve directly and compose.
                // Iterated through the self.result and resolve the fields by recursively calling the `resolve()` function 
                // while updating the path.

                string[] currentPath = self.currentPath.clone();
                json pointer = self.result;
                string pointerType = self.resultType;
                string element = path.shift();
                currentPath.push(element);

                // update the pointer and related information till it finds a @ element.
                while element != "@" {
                    pointer = (<map<json>>pointer).get(element);
                    pointerType = self.queryPlan.get(pointerType).fields.get(element).'type;
                    element = path.shift();
                    currentPath.push(element);
                }

                // Iterate over the list in current pointer and compose the results into the inner fields.
                if pointer is json[] {
                    foreach var i in 0 ..< pointer.length() {
                        Resolver resolver = new (self.queryPlan, pointer[i], pointerType, ['record], currentPath);
                        check resolver.resolve();
                    }
                } else {
                    return error("Error: Cannot resolve the field.");
                }
            }

        }
    }

    // helper functions.

    // Compose results to the final result. i.e. to the `result` object.
    isolated function compose(json finalResult, json resultToCompose, string[] path) returns error? {
        string[] pathCopy = path.clone();
        json pointer = finalResult;
        string element = pathCopy.shift();

        while (pathCopy.length() > 0) {
            if element == "@" {
                if resultToCompose is json[] && pointer is json[] {
                    foreach var i in 0 ..< resultToCompose.length() {
                        check self.compose(pointer[i], resultToCompose[i], pathCopy);
                    }
                    return;
                }
                else {
                    // Ideally should not be thrown
                    return error("Error: Cannot compose into the result.");
                }
            }
            else {
                if pointer is map<json> {
                    if (pointer.hasKey(element)) {
                        pointer = pointer.get(element);
                    } else {
                        return error(element.toString() + " is not found in pointer :" + pointer.toString());
                    }
                } else {
                    // Ideally should not be thrown
                    return error("Error: Cannot compose into the result.");
                }
            }
            element = pathCopy.shift();
        }

        if pointer is map<json> {
            if resultToCompose is map<json> {
                pointer[element] = resultToCompose[element];
            } else if resultToCompose is json[] {
                pointer[element] = (<map<json>>resultToCompose[0])[element];
            } else {
                // Ideally should not be thrown
                return error("Error: Cannot compose into the result.");
            }

        } else {
            // Ideally should not be thrown
            return error("Error: Cannot compose into the result.");
        }
    }

    private isolated function getEffectivePath(graphql:Field 'field) returns string[] {
        return convertPathToStringArray('field.getPath().slice(self.currentPath.length()));
    }

    // Get the values of required fields from the results.
    // Don't support @ in the path.
    isolated function getRequiredFieldsInPath(json pointer, string pointerType, string clientName, string[] path, RequiresFieldRecord[]? requiredFields = ()) returns map<json>[]|error {
        if path.length() == 0 {
            string key = self.queryPlan.get(pointerType).keys.get(clientName);

            map<json>[] fields = [];
            if pointer is json[] {
                foreach var element in pointer {
                    map<json> keyField = {};
                    keyField[key] = (<map<json>>element)[key];
                    map<json> fieldValues = check self.fetchRequiredFields(requiredFields, keyField, pointerType);
                    fields.push(fieldValues);
                }
            } else if pointer is map<json> {
                map<json> keyField = {};
                keyField[key] = (<map<json>>pointer)[key];
                map<json> fieldValues = check self.fetchRequiredFields(requiredFields, keyField, pointerType);
                fields.push(fieldValues);
            } else {
                return error("Error: Cannot get ids from the result.");
            }

            return fields;
        }

        string element = path.shift();
        json newPointer = (<map<json>>pointer)[element];
        string fieldType = self.queryPlan.get(pointerType).fields.get(element).'type;

        return self.getRequiredFieldsInPath(newPointer, fieldType, clientName, path, requiredFields);
    }

    // Fetch the fields from subgraphs and add them to the fieldValues map.
    isolated function fetchRequiredFields(RequiresFieldRecord[]? requiresFields, map<json> fieldValues, string typeName) returns map<json>|error {
        if requiresFields is () {
            return fieldValues;
        }

        map<json> newFieldValues = fieldValues.clone();
        foreach RequiresFieldRecord 'record in requiresFields {
            string queryString = wrapWithEntityRepresentation(typeName, [fieldValues], 'record.fieldString);
            graphql:Client 'client = getClient('record.clientName);
            EntityResponse response = check 'client->execute(queryString);
            newFieldValues = check (check value:mergeJson(newFieldValues, response.data._entities[0])).ensureType();
        }
        return newFieldValues;
    }
}
