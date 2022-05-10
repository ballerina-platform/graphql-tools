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