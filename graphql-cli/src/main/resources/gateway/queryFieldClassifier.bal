import ballerina/graphql;

public class QueryFieldClassifier {

    // client for which the field peroperties are classified.
    private string clientName;

    // field properties
    private string fieldTypeName;
    private string fieldName;

    // resolvable fields are pushed to this.
    private graphql:Field[] resolvableFields;

    // unresolvable fields are pushed to the map along with the parentType name.
    // parent type name is needed to decide which type the subfield belongs for.
    private UnResolvableField[] unresolvableFields;

    // Query plan used to classify the fields.
    private final readonly & table<QueryPlanEntry> key(typename) queryPlan;

    public isolated function init(graphql:Field 'field, readonly & table<QueryPlanEntry> key(typename) queryPlan, string clientName) {
        // initialize the class properties.
        self.clientName = clientName;
        self.queryPlan = queryPlan;
        self.resolvableFields = [];
        self.unresolvableFields = [];

        graphql:Field[]? subfields = 'field.getSubfields();

        string? fieldTypeName = 'field.getUnwrappedType().name;

        // Panic if field object has no subfields or the unwrapped type has no name.
        if subfields is () || fieldTypeName is () {
            panic error("Error: Invalid field object");
        }

        self.fieldTypeName = fieldTypeName;
        self.fieldName = 'field.getName();

        // iterate through all the 
        foreach var subfield in subfields {
            if self.isResolvable(subfield, fieldTypeName, clientName) {
                self.resolvableFields.push(subfield);
            }
            else {
                self.unresolvableFields.push({
                    'field: subfield,
                    parent: fieldTypeName
                });
            }

        }
    }

    public isolated function getFieldString() returns string {
        // Return field string that can be fetched from the client given.
        // If no field is availble to fetch with given client return nil.

        string[] properties = [];
        foreach var 'field in self.resolvableFields {
            // if scalar push name to properties array.
            if 'field.getUnwrappedType().kind == "SCALAR" {
                properties.push('field.getName());
            }
            else {
                // Create a new classifier for the field.
                // classify and expand the unResolvableFields with the inner level.
                QueryFieldClassifier classifier = new ('field, self.queryPlan, self.clientName);

                // Get the inner field string and push it to the properties array.
                properties.push(string `${'field.getName()} { ${classifier.getFieldString()} }`);
                UnResolvableField[] fields = classifier.getUnresolvableFields();
                self.unresolvableFields.push(...fields);
            }
        }

        // Push the key field even it is not requested.
        string key = self.queryPlan.get(self.fieldTypeName).keys.get(self.clientName);
        if properties.indexOf(key) is () {
            properties.push(key);
        }

        return string:'join(" ", ...properties);
    }

    public isolated function getFieldStringWithRoot() returns string {
        return string `${self.fieldName} { ${self.getFieldString()} }`;
    }

    public isolated function getResolvableFields() returns graphql:Field[] {
        return self.resolvableFields;
    }

    public isolated function getUnresolvableFields() returns UnResolvableField[] {
        return self.unresolvableFields;
    }

    private isolated function isResolvable(graphql:Field 'field, string parentType, string clientName) returns boolean {
        // check wether the field is the key. Because key SHOULD be resolvable from the client.
        // OR the client name for resolving the field is equal to the given clientName.
        if 'field.getName() == self.queryPlan.get(parentType).keys[clientName] ||
            self.queryPlan.get(parentType).fields.get('field.getName()).'client == clientName {
            return true;
        }
        else {
            return false;
        }
    }

}
