public type Astronaut record {|
    Mission?[]? missions?;
    string? name?;
    string id?;
|};

public type Mission record {|
    string? endDate?;
    string id?;
    string designation?;
    string? startDate?;
    Astronaut?[]? crew?;
|};

public type astronautResponse record {
    record {|Astronaut astronaut;|} data;
};

public type astronautsResponse record {
    record {|Astronaut[] astronauts;|} data;
};

public type missionResponse record {
    record {|Mission mission;|} data;
};

public type missionsResponse record {
    record {|Mission[] missions;|} data;
};
