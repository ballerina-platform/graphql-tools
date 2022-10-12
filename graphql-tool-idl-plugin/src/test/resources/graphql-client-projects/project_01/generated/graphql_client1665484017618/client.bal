import ballerina/http;
import ballerina/graphql;

public isolated client class 'client {
    final graphql:Client graphqlClient;
    public isolated function init(string serviceUrl, ConnectionConfig config) returns graphql:ClientError? {
        http:ClientConfiguration httpClientConfig = {httpVersion: config.httpVersion, http1Settings: {...config.http1Settings}, http2Settings: config.http2Settings, timeout: config.timeout, forwarded: config.forwarded, poolConfig: config.poolConfig, cache: config.cache, compression: config.compression, circuitBreaker: config.circuitBreaker, retryConfig: config.retryConfig, responseLimits: config.responseLimits, secureSocket: config.secureSocket, proxy: config.proxy, validation: config.validation};
        graphql:Client clientEp = check new (serviceUrl, httpClientConfig);
        self.graphqlClient = clientEp;
    }
    remote isolated function countryByCode(string code) returns CountryByCodeResponse|graphql:ClientError {
        string query = string `query countryByCode($code:ID!) {country(code:$code) {name}}`;
        map<anydata> variables = {"code": code};
        json graphqlResponse = check self.graphqlClient->executeWithType(query, variables);
        return <CountryByCodeResponse> check performDataBinding(graphqlResponse, CountryByCodeResponse);
    }
}
