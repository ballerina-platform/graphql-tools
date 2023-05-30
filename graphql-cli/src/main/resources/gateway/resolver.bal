import ballerina/graphql;

public class Resolver {

    private UnresolvableField[] toBeResolved;

    // The final result of the resolver. Created an composed while resolving by `resolve()`.
    private json result;
    private string resultType;
    private string[] currentPath;

    // Query plan used to classify the fields.
    private final readonly & table<QueryPlanEntry> key(typename) queryPlan;

    public isolated function init(readonly & table<QueryPlanEntry> key(typename) queryPlan, json result, string resultType, UnresolvableField[] unResolvableFields, string[] currentPath) {
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
            UnresolvableField 'record = self.toBeResolved.shift();
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

                map<json>[] keyFieldsWithValues = check self.getKeyFieldsWithValues(self.result, self.resultType, clientName, path);

                if getOfType('record.'field.getType()).kind == "SCALAR" {
                    // If the field type is a scalar type, just pass the field name wrapped with entity representation.
                    string queryString = wrapWithEntityRepresentation('record.parent, keyFieldsWithValues, 'record.'field.getName());
                    EntityResponse result = check 'client->execute(queryString);
                    check self.compose(self.result, result.data._entities, self.getEffectivePath('record.'field));
                } else {
                    // Else need to classify the fields and resolve them accordingly.
                    QueryFieldClassifier classifier = new ('record.'field, self.queryPlan, clientName);
                    string fieldString = classifier.getFieldStringWithRoot();
                    string queryString = wrapWithEntityRepresentation('record.parent, keyFieldsWithValues, fieldString);
                    EntityResponse response = check 'client->execute(queryString);
                    check self.compose(self.result, response.data._entities, self.getEffectivePath('record.'field));
                    UnresolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();
                    if propertiesNotResolved.length() > 0 {
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

    // Compose results to the final result. i.e. to the `result` object.
    isolated function compose(json finalResult, json resultToCompose, string[] path) returns error? {
        string[] pathCopy = path.clone();
        json pointer = finalResult;
        string element = pathCopy.shift();

        while pathCopy.length() > 0 {
            if element == "@" {
                if resultToCompose is json[] && pointer is json[] {
                    foreach var i in 0 ..< resultToCompose.length() {
                        check self.compose(pointer[i], resultToCompose[i], pathCopy);
                    }
                    return;
                }
                // Ideally should not be thrown
                return error("Error: Cannot compose into the result.");
            }
            else {
                if pointer is map<json> {
                    if pointer.hasKey(element) {
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
    isolated function getKeyFieldsWithValues(json pointer, string pointerType, string clientName, string[] path) returns map<json>[]|error {
        if path.length() == 0 {
            string key = self.queryPlan.get(pointerType).keys.get(clientName);

            map<json>[] fields = [];
            if pointer is json[] {
                foreach var element in pointer {
                    map<json> keyField = {};
                    keyField[key] = (<map<json>>element)[key];
                    fields.push(keyField);
                }
            } else if pointer is map<json> {
                map<json> keyField = {};
                keyField[key] = (<map<json>>pointer)[key];
                fields.push(keyField);
            } else {
                return error("Error: Cannot get key field from the result.");
            }

            return fields;
        }

        string element = path.shift();
        json newPointer = (<map<json>>pointer)[element];
        string fieldType = self.queryPlan.get(pointerType).fields.get(element).'type;

        return self.getKeyFieldsWithValues(newPointer, fieldType, clientName, path);
    }
}
