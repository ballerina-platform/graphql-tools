import ballerina/http;
import ballerina/graphql;

public isolated client class CountryqueriesClient {
    final graphql:Client graphqlClient;
    public isolated function init(string serviceUrl, http:ClientConfiguration clientConfig = {}) returns graphql:ClientError? {
        graphql:Client clientEp = check new (serviceUrl, clientConfig);
        self.graphqlClient = clientEp;
        return;
    }
    remote isolated function country(string code) returns CountryResponse|graphql:ClientError {
        string query = string `query country($code:ID!) {country(code:$code) {capital name}}`;
        map<anydata> variables = {"code": code};
        json graphqlResponse = check self.graphqlClient->executeWithType(query, variables);
        return <CountryResponse> check performDataBinding(graphqlResponse, CountryResponse);
    }
    remote isolated function countries(CountryFilterInput? filter = ()) returns CountriesResponse|graphql:ClientError {
        string query = string `query countries($filter:CountryFilterInput) {countries(filter:$filter) {name continent {countries {name}}}}`;
        map<anydata> variables = {"filter": filter};
        json graphqlResponse = check self.graphqlClient->executeWithType(query, variables);
        return <CountriesResponse> check performDataBinding(graphqlResponse, CountriesResponse);
    }
    remote isolated function combinedQuery(string code, CountryFilterInput? filter = ()) returns CombinedQueryResponse|graphql:ClientError {
        string query = string `query combinedQuery($code:ID!,$filter:CountryFilterInput) {country(code:$code) {name} countries(filter:$filter) {name continent {countries {continent {name}}}}}`;
        map<anydata> variables = {"filter": filter, "code": code};
        json graphqlResponse = check self.graphqlClient->executeWithType(query, variables);
        return <CombinedQueryResponse> check performDataBinding(graphqlResponse, CombinedQueryResponse);
    }
    remote isolated function neighbouringCountries() returns NeighbouringCountriesResponse|graphql:ClientError {
        string query = string `query neighbouringCountries {countries(filter:{code:{eq:"LK"}}) {name continent {countries {name}}}}`;
        map<anydata> variables = {};
        json graphqlResponse = check self.graphqlClient->executeWithType(query, variables);
        return <NeighbouringCountriesResponse> check performDataBinding(graphqlResponse, NeighbouringCountriesResponse);
    }
}
