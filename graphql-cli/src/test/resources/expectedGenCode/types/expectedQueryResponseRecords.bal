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