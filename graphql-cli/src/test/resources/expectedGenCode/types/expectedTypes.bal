# Represents ContinentFilterInput
public type ContinentFilterInput record {
    StringQueryOperatorInput? code?;
};

# Represents CountryFilterInput
public type CountryFilterInput record {
    StringQueryOperatorInput? continent?;
    StringQueryOperatorInput? code?;
    StringQueryOperatorInput? currency?;
};

# Represents LanguageFilterInput
public type LanguageFilterInput record {
    StringQueryOperatorInput? code?;
};

# Represents StringQueryOperatorInput
public type StringQueryOperatorInput record {
    string?[]? nin?;
    string? regex?;
    string? ne?;
    string? glob?;
    string? eq?;
    string?[]? 'in?;
};

# Represents CountryResponse
public type CountryResponse record {|
    map<json?> __extensions?;
    record {|
        string name;
    |}? country;
|};

# Represents CountriesResponse
public type CountriesResponse record {|
    map<json?> __extensions?;
    record {|
        string name;
        record {|
            record {|
                string name;
            |}[] countries;
        |} continent;
    |}[] countries;
|};

# Represents CombinedQueryResponse
public type CombinedQueryResponse record {|
    map<json?> __extensions?;
    record {|
        string name;
    |}? country;
    record {|
        string name;
        record {|
            record {|
                record {|
                    string name;
                |} continent;
            |}[] countries;
        |} continent;
    |}[] countries;
|};

# Represents NeighbouringCountriesResponse
public type NeighbouringCountriesResponse record {|
    map<json?> __extensions?;
    record {|
        string name;
        record {|
            record {|
                string name;
            |}[] countries;
        |} continent;
    |}[] countries;
|};

# Represents AliasExample1Response
public type AliasExample1Response record {|
    map<json?> __extensions?;
    record {|
        string code;
        string name;
    |}? lk;
    record {|
        string code;
        string native;
    |}? au;
    record {|
        string code;
        string name;
    |}? continent;
|};