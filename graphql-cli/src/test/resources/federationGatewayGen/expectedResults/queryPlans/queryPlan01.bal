public const string ASTRONAUTS = "astronauts";
public const string MISSIONS = "missions";

public final readonly & table<queryPlanEntry> key(typename) queryPlan = table [
    {
        typename: "Astronaut",
        keys: {
            "astronauts": "id",
            "missions": "id"
        },
        fields: table [
            {name: "name", 'type: "STRING", 'client: ASTRONAUTS},
            {name: "missions", 'type: "Mission", 'client: MISSIONS}
        ]
    },
    {
        typename: "Mission",
        keys: {
            "missions": "id"
        },
        fields: table [
            {name: "designation", 'type: "STRING", 'client: MISSIONS},
            {name: "crew", 'type: "Astronaut", 'client: MISSIONS},
            {name: "startDate", 'type: "STRING", 'client: MISSIONS},
            {name: "endDate", 'type: "STRING", 'client: MISSIONS}
        ]
    }
];