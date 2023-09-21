public type ContinentFilterInput record {
    StringQueryOperatorInput? code?;
};

public type CountryFilterInput record {
    StringQueryOperatorInput? continent?;
    StringQueryOperatorInput? code?;
    StringQueryOperatorInput? name?;
    StringQueryOperatorInput? currency?;
};

public type LanguageFilterInput record {
    StringQueryOperatorInput? code?;
};

public type StringQueryOperatorInput record {
    string[]? nin?;
    string? regex?;
    string? ne?;
    string? eq?;
    string[]? 'in?;
};

public type CountryResponse record {|
    map<json?> __extensions?;
    record {|
        string? capital;
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
