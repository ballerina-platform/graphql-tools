# Represents CustomInput
public type CustomInput record {
    string? field1?;
    CustomInput1? field2?;
};

# Represents CustomInput1
public type CustomInput1 record {
    string? field1?;
    string field2?;
};

# Represents Operation1Response
type Operation1Response record {|
    map<json?> __extensions?;
    record {|
        string? field1;
        string field2;
    |}? operation1;
|};

# Represents Operation2Response
type Operation2Response record {|
    map<json?> __extensions?;
    record {|
        string? field1;
        string field2;
    |}? operation2;
|};

# Represents Operation3Response
type Operation3Response record {|
    map<json?> __extensions?;
    record {|
        string? field1;
        string field2;
    |}? operation3;
|};
