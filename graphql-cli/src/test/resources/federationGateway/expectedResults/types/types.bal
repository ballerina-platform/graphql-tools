public type Astronaut record {|
    Mission[] missions?;
    string name?;
    int id?;
|};

public type Mission record {|
    string? endDate?;
    int id?;
    string designation?;
    string? startDate?;
    Astronaut[] crew?;
|};

public type MissionInput record {|
    int[] crewIds;
    string? endDate;
    string designation;
    string? startDate;
|};

public type astronautsResponse record {
    record {|Astronaut[] astronauts;|} data;
};

public type astronautResponse record {
    record {|Astronaut astronaut;|} data;
};

public type missionsResponse record {
    record {|Mission[] missions;|} data;
};

public type missionResponse record {
    record {|Mission mission;|} data;
};

public type addMissionResponse record {
    record {|Mission addMission;|} data;
};
