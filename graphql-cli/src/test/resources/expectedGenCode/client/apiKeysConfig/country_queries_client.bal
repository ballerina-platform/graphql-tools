import ballerina/http;
import ballerinax/graphql;

public type ApiKeysConfig record {|
    string header1;
    string header2;
|};

public isolated client class CountryqueriesClient {
    final graphql:Client graphqlClient;
    final readonly & ApiKeysConfig apiKeysConfig;
    public isolated function init(ApiKeysConfig apiKeysConfig, string serviceUrl, http:ClientConfiguration clientConfig = {}) returns graphql:Error? {
        graphql:Client clientEp = check new (serviceUrl, clientConfig);
        self.graphqlClient = clientEp;
        self.apiKeysConfig = apiKeysConfig.cloneReadOnly();
        return;
    }
    remote isolated function country(string code) returns CountryResponse|graphql:Error {
        string query = string `query country($code:ID!) {country(code:$code) {capital name}}`;
        map<anydata> variables = {"code": code};
        map<any> headerValues = {"Header1": self.apiKeysConfig.header1, "Header2": self.apiKeysConfig.header2};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        json graphqlResponse = check self.graphqlClient->executeWithType(query, variables, httpHeaders);
        return <CountryResponse> check performDataBinding(graphqlResponse, CountryResponse);
    }
    remote isolated function countries(CountryFilterInput? filter = ()) returns CountriesResponse|graphql:Error {
        string query = string `query countries($filter:CountryFilterInput) {countries(filter:$filter) {name continent {countries {name}}}}`;
        map<anydata> variables = {"filter": filter};
        map<any> headerValues = {"Header1": self.apiKeysConfig.header1, "Header2": self.apiKeysConfig.header2};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        json graphqlResponse = check self.graphqlClient->executeWithType(query, variables, httpHeaders);
        return <CountriesResponse> check performDataBinding(graphqlResponse, CountriesResponse);
    }
    remote isolated function combinedQuery(string code, CountryFilterInput? filter = ()) returns CombinedQueryResponse|graphql:Error {
        string query = string `query combinedQuery($code:ID!,$filter:CountryFilterInput) {country(code:$code) {name} countries(filter:$filter) {name continent {countries {continent {name}}}}}`;
        map<anydata> variables = {"filter": filter, "code": code};
        map<any> headerValues = {"Header1": self.apiKeysConfig.header1, "Header2": self.apiKeysConfig.header2};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        json graphqlResponse = check self.graphqlClient->executeWithType(query, variables, httpHeaders);
        return <CombinedQueryResponse> check performDataBinding(graphqlResponse, CombinedQueryResponse);
    }
    remote isolated function neighbouringCountries() returns NeighbouringCountriesResponse|graphql:Error {
        string query = string `query neighbouringCountries {countries(filter:{code:{eq:"LK"}}) {name continent {countries {name}}}}`;
        map<anydata> variables = {};
        map<any> headerValues = {"Header1": self.apiKeysConfig.header1, "Header2": self.apiKeysConfig.header2};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        json graphqlResponse = check self.graphqlClient->executeWithType(query, variables, httpHeaders);
        return <NeighbouringCountriesResponse> check performDataBinding(graphqlResponse, NeighbouringCountriesResponse);
    }
}
