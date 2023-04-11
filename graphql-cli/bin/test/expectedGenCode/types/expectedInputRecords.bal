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