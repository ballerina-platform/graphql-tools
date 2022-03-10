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
        return <CountryResponse> check self.graphqlClient->execute(CountryResponse, query, variables, httpHeaders);
    }
    remote isolated function countries(CountryFilterInput? filter = ()) returns CountriesResponse|graphql:Error {
        string query = string `query countries($filter:CountryFilterInput) {countries(filter:$filter) {name continent {countries {name}}}}`;
        map<anydata> variables = {"filter": filter};
        map<any> headerValues = {"Header1": self.apiKeysConfig.header1, "Header2": self.apiKeysConfig.header2};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        return <CountriesResponse> check self.graphqlClient->execute(CountriesResponse, query, variables, httpHeaders);
    }
    remote isolated function combinedQuery(string code, CountryFilterInput? filter = ()) returns CombinedQueryResponse|graphql:Error {
        string query = string `query combinedQuery($code:ID!,$filter:CountryFilterInput) {country(code:$code) {name} countries(filter:$filter) {name continent {countries {continent {name}}}}}`;
        map<anydata> variables = {"filter": filter, "code": code};
        map<any> headerValues = {"Header1": self.apiKeysConfig.header1, "Header2": self.apiKeysConfig.header2};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        return <CombinedQueryResponse> check self.graphqlClient->execute(CombinedQueryResponse, query, variables, httpHeaders);
    }
    remote isolated function neighbouringCountries() returns NeighbouringCountriesResponse|graphql:Error {
        string query = string `query neighbouringCountries {countries(filter:{code:{eq:"LK"}}) {name continent {countries {name}}}}`;
        map<anydata> variables = {};
        map<any> headerValues = {"Header1": self.apiKeysConfig.header1, "Header2": self.apiKeysConfig.header2};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        return <NeighbouringCountriesResponse> check self.graphqlClient->execute(NeighbouringCountriesResponse, query, variables, httpHeaders);
    }
}
