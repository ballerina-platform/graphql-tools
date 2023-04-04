import ballerina/graphql;

final graphql:Client astronautClient = check new graphql:Client("http://localhost:4001");
final graphql:Client missionClient = check new graphql:Client("http://localhost:4002");

isolated function getClient(string clientName) returns graphql:Client {
    if (clientName == "astronauts") {
        return astronautClient;
    } else {
        return missionClient;
    }
}

isolated service on new graphql:Listener(9000) {

    isolated resource function get astronaut(int id, graphql:Field 'field) returns Astronaut|error {
        QueryFieldClassifier classifier = new ('field, queryPlan, ASTRONAUTS);

        string fieldString = classifier.getFieldString();
        unResolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();

        string queryString = wrapwithQuery("astronaut", fieldString, {"id": id.toString()});
        AstronautResponse response = check astronautClient->execute(queryString);

        Astronaut result = response.data.astronaut;

        Resolver resolver = new (queryPlan, result, "Astronaut", propertiesNotResolved, ["astronaut"]);
        return resolver.getResult().ensureType();
    }

    isolated resource function get astronauts(graphql:Field 'field) returns Astronaut[]|error {
        QueryFieldClassifier classifier = new ('field, queryPlan, ASTRONAUTS);

        string fieldString = classifier.getFieldString();
        unResolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();

        string queryString = wrapwithQuery("astronauts", fieldString);
        AstronautsResponse response = check astronautClient->execute(queryString);

        Astronaut[] result = response.data.astronauts;

        Resolver resolver = new (queryPlan, result, "Astronaut", propertiesNotResolved, ["astronauts"]);
        return resolver.getResult().ensureType();

    }

    isolated resource function get mission(int id, graphql:Field 'field) returns Mission|error {
        graphql:Client 'client = getClient(MISSIONS);
        QueryFieldClassifier classifier = new ('field, queryPlan, MISSIONS);

        string fieldString = classifier.getFieldString();
        unResolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();

        string queryString = wrapwithQuery("mission", fieldString, {"id": id.toString()});
        MissionResponse response = check 'client->execute(queryString);

        Mission result = response.data.mission;

        Resolver resolver = new (queryPlan, result, "Mission", propertiesNotResolved, ["mission"]);
        return resolver.getResult().ensureType();

    }

    isolated resource function get missions(graphql:Field 'field) returns Mission[]|error {
        graphql:Client 'client = getClient(MISSIONS);
        QueryFieldClassifier classifier = new ('field, queryPlan, MISSIONS);

        string fieldString = classifier.getFieldString();
        unResolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();

        string queryString = wrapwithQuery("missions", fieldString);
        MissionsResponse response = check 'client->execute(queryString);

        Mission[] result = response.data.missions;

        Resolver resolver = new (queryPlan, result, "Mission", propertiesNotResolved, ["missions"]);
        return resolver.getResult().ensureType();
    }

}