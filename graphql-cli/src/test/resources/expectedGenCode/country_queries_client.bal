import ballerina/http;
import ballerinax/graphql;

public isolated client class CountryqueriesClient {
    final graphql:Client graphqlClient;
    public isolated function init(string serviceUrl, http:ClientConfiguration clientConfig = {}) returns graphql:Error? {
        graphql:Client clientEp = check new (serviceUrl, clientConfig);
        self.graphqlClient = clientEp;
        return;
    }
    remote isolated function country(string code) returns CountryResponse|graphql:Error {
        string query = string `query country($code:ID!) {country(code:$code) {capital name}}`;
        map<anydata> variables = {"code": code};
        return <CountryResponse> check self.graphqlClient->execute(CountryResponse, query, variables);
    }
    remote isolated function countries(CountryFilterInput? filter = ()) returns CountriesResponse|graphql:Error {
        string query = string `query countries($filter:CountryFilterInput) {countries(filter:$filter) {name continent {countries {name}}}}`;
        map<anydata> variables = {"filter": filter};
        return <CountriesResponse> check self.graphqlClient->execute(CountriesResponse, query, variables);
    }
    remote isolated function combinedQuery(string code, CountryFilterInput? filter = ()) returns CombinedQueryResponse|graphql:Error {
        string query = string `query combinedQuery($code:ID!,$filter:CountryFilterInput) {country(code:$code) {name} countries(filter:$filter) {name continent {countries {continent {name}}}}}`;
        map<anydata> variables = {"filter": filter, "code": code};
        return <CombinedQueryResponse> check self.graphqlClient->execute(CombinedQueryResponse, query, variables);
    }
    remote isolated function neighbouringCountries() returns NeighbouringCountriesResponse|graphql:Error {
        string query = string `query neighbouringCountries {countries(filter:{code:{eq:"LK"}}) {name continent {countries {name}}}}`;
        map<anydata> variables = {};
        return <NeighbouringCountriesResponse> check self.graphqlClient->execute(NeighbouringCountriesResponse, query, variables);
    }
}
