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