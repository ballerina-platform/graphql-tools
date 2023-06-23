import ballerina/graphql;

type SchemaWithAddedInputFieldsIntoTypesWithReadonlyInputTypesApi service object {
    *graphql:Service;
    resource function get books() returns Book[]?;
};

public type Book readonly & record {|
    readonly int id;
    string title;
    float price;
|};
