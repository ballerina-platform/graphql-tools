public type ContinentFilterInput record {
    StringQueryOperatorInput? code?;
};

public type CountryFilterInput record {
    StringQueryOperatorInput? continent?;
    StringQueryOperatorInput? code?;
    StringQueryOperatorInput? currency?;
};

public type LanguageFilterInput record {
    StringQueryOperatorInput? code?;
};

public type StringQueryOperatorInput record {
    string?[]? nin?;
    string? regex?;
    string? ne?;
    string? glob?;
    string? eq?;
    string?[]? 'in?;
};

public type CountryResponse record {|
    map<json?> __extensions?;
    record {|
        string name;
    |}? country;
|};

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