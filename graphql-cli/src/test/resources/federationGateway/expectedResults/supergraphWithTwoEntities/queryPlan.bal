public const string MISSIONS = "missions";
public const string ASTRONAUTS = "astronauts";
public final readonly & table<QueryPlanEntry> key(typename) queryPlan = table [
    {typename: "Astronaut", keys: {"astronauts": "id", "missions": "id"}, fields: table [
            {name: "missions", 'type: "Mission", 'client: MISSIONS},
            {name: "name", 'type: "String", 'client: ASTRONAUTS}
        ]},
    {typename: "Mission", keys: {"missions": "id"}, fields: table [
            {name: "endDate", 'type: "String", 'client: MISSIONS},
            {name: "id", 'type: "Int", 'client: MISSIONS},
            {name: "designation", 'type: "String", 'client: MISSIONS},
            {name: "startDate", 'type: "String", 'client: MISSIONS},
            {name: "crew", 'type: "Astronaut", 'client: MISSIONS}
        ]}
];
