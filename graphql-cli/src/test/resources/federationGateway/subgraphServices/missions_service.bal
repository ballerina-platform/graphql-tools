import ballerina/graphql;
import ballerina/graphql.subgraph;

@subgraph:Subgraph
isolated service on new graphql:Listener(5002) {
    isolated resource function get missions() returns Mission[] {
        return missions;
    }
    isolated resource function get mission(int id) returns Mission {
        return missions.filter(isolated function(Mission mission) returns boolean {
            return mission.id == id;
        })[0];
    }
}

@subgraph:Entity {
    'key: "id"
}
distinct isolated service readonly class Mission {
    public final int id;
    private final string designation;
    private final string? startDate;
    private final string? endDate;
    private final readonly & int[] crewIds;

    isolated function init(int id, string designation, string? startDate, string? endDate, readonly & int[] crewIds) {
        self.id = id;
        self.designation = designation;
        self.startDate = startDate;
        self.endDate = endDate;
        self.crewIds = crewIds;
    }

    isolated resource function get id() returns int {
        return self.id;
    }

    isolated resource function get designation() returns string {
        return self.designation;
    }

    isolated resource function get startDate() returns string? {
        return self.startDate;
    }

    isolated resource function get endDate() returns string? {
        return self.endDate;
    }

    isolated resource function get crew() returns Astronaut[] {
        return self.crewIds.map(isolated function(int id) returns Astronaut {
            return new (id);
        });
    }

    public isolated function includes(int id) returns boolean {
        return self.crewIds.indexOf(id) != ();
    }

}

@subgraph:Entity {
    'key: "id",
    resolveReference: isolated function(subgraph:Representation representation) returns Astronaut?|error {
        int id = check representation["id"].ensureType();
        return new (id);
    }
}
distinct isolated service readonly class Astronaut {
    private final int id;
    isolated function init(int id) {
        self.id = id;
    }

    isolated resource function get id() returns int {
        return self.id;
    }

    isolated resource function get missions() returns Mission[] {
        final int id = self.id;
        return missions.filter(isolated function(Mission mission) returns boolean {
            return mission.includes(id);
        });
    }
}

final readonly & Mission[] missions = [
    new Mission(1, "Apollo 1", (), (), [14, 30, 7]),
    new Mission(2, "Apollo 4", "1967-11-09T12:00:01.000Z", "1967-11-09T20:37:00.000Z", []),
    new Mission(3, "Apollo 5", "1968-01-22T22:48:09.000Z", "1968-01-23T09:58:00.000Z", []),
    new Mission(4, "Apollo 6", "1968-04-04T12:00:01.000Z", "1968-04-04T21:57:21.000Z", []),
    new Mission(5, "Apollo 7", "1968-10-11T15:02:45.000Z", "1968-10-22T11:11:48.000Z", [23, 10, 12]),
    new Mission(6, "Apollo 8", "1968-12-21T12:51:00.000Z", "1968-12-27T15:51:42.000Z", [5, 18, 2]),
    new Mission(7, "Apollo 9", "1969-03-03T16:00:00.000Z", "1969-03-13T17:00:54.000Z", [20, 26, 25]),
    new Mission(8, "Apollo 10", "1969-05-18T16:49:00.000Z", "1969-05-26T16:52:23.000Z", [28, 31, 6]),
    new Mission(9, "Apollo 11", "1969-07-16T13:32:00.000Z", "1969-07-24T16:50:35.000Z", [3, 1, 8]),
    new Mission(10, "Apollo 12", "1969-11-14T16:22:00.000Z", "1969-11-24T20:58:24.000Z", [9, 15, 4]),
    new Mission(11, "Apollo 13", "1970-04-11T19:13:00.000Z", "1970-04-17T18:07:41.000Z", [18, 29, 16]),
    new Mission(12, "Apollo 14", "1971-01-31T21:03:02.000Z", "1971-02-09T21:05:00.000Z", [27, 22, 21]),
    new Mission(13, "Apollo 15", "1971-07-26T13:34:00.600Z", "1971-08-07T20:45:53.000Z", [26, 32, 17]),
    new Mission(14, "Apollo 16", "1972-04-16T17:54:00.000Z", "1972-04-27T19:45:05.000Z", [31, 19, 11]),
    new Mission(15, "Apollo 17", "1972-12-07T05:33:00.000Z", "1972-12-19T19:24:59.000Z", [6, 13, 24])
];
