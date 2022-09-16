import ballerina/http;
import ballerina/graphql;

public isolated client class CountryqueriesClient {
    final graphql:Client graphqlClient;
    final readonly & ApiKeysConfig apiKeysConfig;
    public isolated function init(ConnectionConfig config, ApiKeysConfig apiKeysConfig, string serviceUrl) returns graphql:ClientError? {
        http:ClientConfiguration httpClientConfig = {
            auth: config.auth,
            httpVersion: config.httpVersion,
            http1Settings: {...config.http1Settings},
            http2Settings: config.http2Settings,
            timeout: config.timeout,
            forwarded: config.forwarded,
            poolConfig: config.poolConfig,
            cache: config.cache,
            compression: config.compression,
            circuitBreaker: config.circuitBreaker,
            retryConfig: config.retryConfig,
            responseLimits: config.responseLimits,
            secureSocket: config.secureSocket,
            proxy: config.proxy,
            validation: config.validation
        };
        graphql:Client clientEp = check new (serviceUrl, httpClientConfig);
        self.graphqlClient = clientEp;
        self.apiKeysConfig = apiKeysConfig.cloneReadOnly();
    }
    remote isolated function country(string code) returns CountryResponse|graphql:ClientError {
        string query = string `query country($code:ID!) {country(code:$code) {capital name}}`;
        map<anydata> variables = {"code": code};
        map<any> headerValues = {"Header1": self.apiKeysConfig.header1, "Header2": self.apiKeysConfig.header2};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        json graphqlResponse = check self.graphqlClient->executeWithType(query, variables, headers = httpHeaders);
        return <CountryResponse>check performDataBinding(graphqlResponse, CountryResponse);
    }
    remote isolated function countries(CountryFilterInput? filter = ()) returns CountriesResponse|graphql:ClientError {
        string query = string `query countries($filter:CountryFilterInput) {countries(filter:$filter) {name continent {countries {name}}}}`;
        map<anydata> variables = {"filter": filter};
        map<any> headerValues = {"Header1": self.apiKeysConfig.header1, "Header2": self.apiKeysConfig.header2};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        json graphqlResponse = check self.graphqlClient->executeWithType(query, variables, headers = httpHeaders);
        return <CountriesResponse>check performDataBinding(graphqlResponse, CountriesResponse);
    }
    remote isolated function combinedQuery(string code, CountryFilterInput? filter = ()) returns CombinedQueryResponse|graphql:ClientError {
        string query = string `query combinedQuery($code:ID!,$filter:CountryFilterInput) {country(code:$code) {name} countries(filter:$filter) {name continent {countries {continent {name}}}}}`;
        map<anydata> variables = {"filter": filter, "code": code};
        map<any> headerValues = {"Header1": self.apiKeysConfig.header1, "Header2": self.apiKeysConfig.header2};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        json graphqlResponse = check self.graphqlClient->executeWithType(query, variables, headers = httpHeaders);
        return <CombinedQueryResponse>check performDataBinding(graphqlResponse, CombinedQueryResponse);
    }
    remote isolated function neighbouringCountries() returns NeighbouringCountriesResponse|graphql:ClientError {
        string query = string `query neighbouringCountries {countries(filter:{code:{eq:"LK"}}) {name continent {countries {name}}}}`;
        map<anydata> variables = {};
        map<any> headerValues = {"Header1": self.apiKeysConfig.header1, "Header2": self.apiKeysConfig.header2};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        json graphqlResponse = check self.graphqlClient->executeWithType(query, variables, headers = httpHeaders);
        return <NeighbouringCountriesResponse>check performDataBinding(graphqlResponse, NeighbouringCountriesResponse);
    }
}
