public type CountryFieldsFragment record {|
    string code;
    string name;
|};

public type FragmentExample1Response record {|
    map<json?> __extensions?;
    record {|
        *CountryFieldsFragment;
    |}? a;
    record {|
        *CountryFieldsFragment;
        string native;
    |}? b;
    record {|
        string code;
        string name;
    |}? continent;
|};

public type CountryFields2Fragment record {|
    string code;
    string name;
    record {|
        string name;
        string code;
    |} continent;
    record {|
        string code;
        string? name;
        string? native;
    |}[] languages;
|};

public type FragmentExample2Response record {|
    map<json?> __extensions?;
    record {|
        *CountryFields2Fragment;
    |}? a;
    record {|
        *CountryFields2Fragment;
        string native;
    |}? b;
    record {|
        string code;
        string name;
    |}? continent;
|};

public type CountryFields3Fragment record {|
    string code;
    string name;
    record {|
        string name;
        record {|
            string native;
        |}[] countries;
    |} continent;
|};

public type FragmentExample3Response record {|
    map<json?> __extensions?;
    record {|
        *CountryFields3Fragment;
    |}? a;
    record {|
        *CountryFields3Fragment;
        string native;
    |}? b;
    record {|
        string code;
        string name;
    |}? continent;
|};

public type InlineFragmentExample1Response record {|
    map<json?> __extensions?;
    record {|
        record {|
            string name;
            record {|
                string? capital;
            |} country;
        |}[] states;
        record {|
            string name;
            string code;
        |} continent;
        string? capital;
    |}? a;
    record {|
        string native;
    |}? b;
    record {|
        string code;
        string name;
    |}? continent;
|};

public type InlineFragmentExample2Response record {|
    map<json?> __extensions?;
    record {|
        record {|
            string name;
            record {|
                string? capital;
                record {|
                    string code;
                    string name;
                |} continent;
            |} country;
        |}[] states;
        record {|
            string code;
            string name;
        |} continent;
        string? capital;
    |}? a;
    record {|
        string code;
        string name;
    |}? continent;
|};