import ballerina/graphql;

public isolated client class GraphqlClient {
    final graphql:Client graphqlClient;
    final readonly & ApiKeysConfig apiKeysConfig;
    public isolated function init(ApiKeysConfig apiKeysConfig, string serviceUrl, ConnectionConfig config = {}) returns graphql:ClientError? {
        graphql:ClientConfiguration graphqlClientConfig = {
           timeout: config.timeout,
           forwarded: config.forwarded,
           poolConfig: config.poolConfig,
           compression: config.compression,
           circuitBreaker: config.circuitBreaker,
           retryConfig: config.retryConfig,
           validation: config.validation
        };
        do {
            if config.http1Settings is ClientHttp1Settings {
                ClientHttp1Settings settings = check config.http1Settings.ensureType(ClientHttp1Settings);
                graphqlClientConfig.http1Settings = {...settings};
            }
            if config.cache is graphql:CacheConfig {
                graphqlClientConfig.cache = check config.cache.ensureType(graphql:CacheConfig);
            }
            if config.responseLimits is graphql:ResponseLimitConfigs {
                graphqlClientConfig.responseLimits = check config.responseLimits.ensureType(graphql:ResponseLimitConfigs);
            }
            if config.secureSocket is graphql:ClientSecureSocket {
                graphqlClientConfig.secureSocket = check config.secureSocket.ensureType(graphql:ClientSecureSocket);
            }
            if config.proxy is graphql:ProxyConfig {
                graphqlClientConfig.proxy = check config.proxy.ensureType(graphql:ProxyConfig);
            }
        } on fail var e {
            return <graphql:ClientError> error("GraphQL Client Error", e, body = ());
        }
        graphql:Client clientEp = check new (serviceUrl, graphqlClientConfig);
        self.graphqlClient = clientEp;
        self.apiKeysConfig = apiKeysConfig.cloneReadOnly();
    }
    remote isolated function country(string code) returns CountryResponse|graphql:ClientError {
        string query = string `query country($code:ID!) {country(code:$code) {capital name}}`;
        map<anydata> variables = {"code": code};
        map<any> headerValues = {"Header1": self.apiKeysConfig.header1, "Header2": self.apiKeysConfig.header2};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        json graphqlResponse = check self.graphqlClient->executeWithType(query, variables, headers = httpHeaders);
        return <CountryResponse> check performDataBinding(graphqlResponse, CountryResponse);
    }
    remote isolated function countries(CountryFilterInput? filter = ()) returns CountriesResponse|graphql:ClientError {
        string query = string `query countries($filter:CountryFilterInput) {countries(filter:$filter) {name continent {countries {name}}}}`;
        map<anydata> variables = {"filter": filter};
        map<any> headerValues = {"Header1": self.apiKeysConfig.header1, "Header2": self.apiKeysConfig.header2};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        json graphqlResponse = check self.graphqlClient->executeWithType(query, variables, headers = httpHeaders);
        return <CountriesResponse> check performDataBinding(graphqlResponse, CountriesResponse);
    }
    remote isolated function combinedQuery(string code, CountryFilterInput? filter = ()) returns CombinedQueryResponse|graphql:ClientError {
        string query = string `query combinedQuery($code:ID!,$filter:CountryFilterInput) {country(code:$code) {name} countries(filter:$filter) {name continent {countries {continent {name}}}}}`;
        map<anydata> variables = {"filter": filter, "code": code};
        map<any> headerValues = {"Header1": self.apiKeysConfig.header1, "Header2": self.apiKeysConfig.header2};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        json graphqlResponse = check self.graphqlClient->executeWithType(query, variables, headers = httpHeaders);
        return <CombinedQueryResponse> check performDataBinding(graphqlResponse, CombinedQueryResponse);
    }
    remote isolated function neighbouringCountries() returns NeighbouringCountriesResponse|graphql:ClientError {
        string query = string `query neighbouringCountries {countries(filter:{code:{eq:"LK"}}) {name continent {countries {name}}}}`;
        map<anydata> variables = {};
        map<any> headerValues = {"Header1": self.apiKeysConfig.header1, "Header2": self.apiKeysConfig.header2};
        map<string|string[]> httpHeaders = getMapForHeaders(headerValues);
        json graphqlResponse = check self.graphqlClient->executeWithType(query, variables, headers = httpHeaders);
        return <NeighbouringCountriesResponse> check performDataBinding(graphqlResponse, NeighbouringCountriesResponse);
    }
}
