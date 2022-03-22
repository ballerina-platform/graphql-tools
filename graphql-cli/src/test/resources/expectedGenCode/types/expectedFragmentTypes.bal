# Represents CountryFieldsFragment
public type CountryFieldsFragment record {|
    string code;
    string name;
|};

# Represents FragmentExample1Response
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

# Represents CountryFields2Fragment
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

# Represents FragmentExample2Response
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

# Represents CountryFields3Fragment
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

# Represents FragmentExample3Response
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

# Represents InlineFragmentExample1Response
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

# Represents InlineFragmentExample2Response
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