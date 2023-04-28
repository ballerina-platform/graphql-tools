import ballerina/graphql;
import ballerina/graphql.subgraph;

@subgraph:Subgraph
isolated service on new graphql:Listener(5001) {
    isolated resource function get astronauts() returns Astronaut[] {
        return astronauts;
    }

    isolated resource function get astronaut(int id) returns Astronaut? {
        return getAstronaut(id);
    }
}

@subgraph:Entity {
    'key: "id",
    resolveReference: isolated function(subgraph:Representation representation) returns Astronaut?|error {
        int id = check representation["id"].ensureType();
        return getAstronaut(id);
    }
}
type Astronaut record {|
    int id;
    string name;
|};

isolated function getAstronaut(int id) returns Astronaut? {
    foreach var astronaut in astronauts {
        if (astronaut.id == id) {
            return astronaut;
        }
    }
    return ();
}

final readonly & Astronaut[] astronauts = [
    {
        "id": 1,
        "name": "Buzz Aldrin"
    },
    {
        "id": 2,
        "name": "William Anders"
    },
    {
        "id": 3,
        "name": "Neil Armstrong"
    },
    {
        "id": 4,
        "name": "Alan Bean"
    },
    {
        "id": 5,
        "name": "Frank Borman"
    },
    {
        "id": 6,
        "name": "Eugene Cernan"
    },
    {
        "id": 7,
        "name": "Roger B. Chaffee"
    },
    {
        "id": 8,
        "name": "Michael Collins"
    },
    {
        "id": 9,
        "name": "C. 'Pete' Conrad"
    },
    {
        "id": 10,
        "name": "Walt Cunningham"
    },
    {
        "id": 11,
        "name": "Charles Duke"
    },
    {
        "id": 12,
        "name": "Donn Eisele"
    },
    {
        "id": 13,
        "name": "Ronald Evans"
    },
    {
        "id": 14,
        "name": "Gus Grissom"
    },
    {
        "id": 15,
        "name": "Richard Gordon"
    },
    {
        "id": 16,
        "name": "Fred Haise"
    },
    {
        "id": 17,
        "name": "James Irwin"
    },
    {
        "id": 18,
        "name": "James Lovell"
    },
    {
        "id": 19,
        "name": "T. Kenneth Mattingly"
    },
    {
        "id": 20,
        "name": "James McDivitt"
    },
    {
        "id": 21,
        "name": "Edgar Mitchell"
    },
    {
        "id": 22,
        "name": "Stuart Roosa"
    },
    {
        "id": 23,
        "name": "Wally Schirra"
    },
    {
        "id": 24,
        "name": "Harrison Schmitt"
    },
    {
        "id": 25,
        "name": "Russell Schweickart"
    },
    {
        "id": 26,
        "name": "David Scott"
    },
    {
        "id": 27,
        "name": "Alan Shepard"
    },
    {
        "id": 28,
        "name": "Thomas Stafford"
    },
    {
        "id": 29,
        "name": "Jack Swigert"
    },
    {
        "id": 30,
        "name": "Ed White"
    },
    {
        "id": 31,
        "name": "John Young"
    },
    {
        "id": 32,
        "name": "Alfred Worden"
    }
];
