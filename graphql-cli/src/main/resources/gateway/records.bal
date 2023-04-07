import ballerina/graphql;

public type UnResolvableField record {|
    string parent;
    graphql:Field 'field;
|};

public type RequiresFieldRecord record {|
    string clientName;
    string fieldString;
|};

public type FieldRecord record {|
    readonly string name;
    string 'type;
    string 'client;
    // In query plan generation need to process the required field string and seperate the fields and the client
    // which will resolve it.
    RequiresFieldRecord[] requires?;
|};

public type QueryPlanEntry record {|
    readonly string typename;
    map<string> keys;
    readonly & table<FieldRecord> key(name) fields;
|};

type EntityResponse record {
    record {|json[] _entities;|} data;
};
