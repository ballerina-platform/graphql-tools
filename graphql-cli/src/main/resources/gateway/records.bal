import ballerina/graphql;

public type UnResolvableField record {|
    string parent;
    graphql:Field 'field;
|};

public type FieldRecord record {|
    readonly string name;
    string 'type;
    string 'client;
|};

public type QueryPlanEntry record {|
    readonly string typename;
    map<string> keys;
    readonly & table<FieldRecord> key(name) fields;
|};

type EntityResponse record {
    record {|json[] _entities;|} data;
};
