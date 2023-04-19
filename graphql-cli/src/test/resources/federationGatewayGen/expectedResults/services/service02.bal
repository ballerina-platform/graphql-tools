import ballerina/graphql;
import ballerina/log;

final graphql:Client REVIEWS_CLIENT = check new graphql:Client("http://localhost:4002");
final graphql:Client INVENTORY_CLIENT = check new graphql:Client("http://localhost:4004");
final graphql:Client ACCOUNTS_CLIENT = check new graphql:Client("http://localhost:4001");
final graphql:Client PRODUCTS_CLIENT = check new graphql:Client("http://localhost:4003");

isolated function getClient(string clientName) returns graphql:Client {
    match clientName {
        "reviews" => {
            return REVIEWS_CLIENT;
        }
        "inventory" => {
            return INVENTORY_CLIENT;
        }
        "accounts" => {
            return ACCOUNTS_CLIENT;
        }
        "products" => {
            return PRODUCTS_CLIENT;
        }
        _ => {
            panic error("Client not found");
        }
    }
}

configurable int PORT = 9000;

isolated service on new graphql:Listener(PORT) {
    isolated function init() {
        log:printInfo(string `💃 Server ready at port: ${PORT}`);
    }

    private isolated function getParamAsString(any param) returns string {
        if param is string {
            return "\"" + param + "\"";
        } else {
            return param.toString();
        }
    }

    isolated resource function get me(graphql:Field 'field) returns User|error {
        QueryFieldClassifier classifier = new ('field, queryPlan, ACCOUNTS);
        string fieldString = classifier.getFieldString();
        UnResolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();
        string queryString = wrapwithQuery("me", fieldString);
        meResponse response = check ACCOUNTS_CLIENT->execute(queryString);
        User result = response.data.me;
        Resolver resolver = new (queryPlan, result, "User", propertiesNotResolved, ["me"]);
        return resolver.getResult().ensureType();
    }
    isolated resource function get topProducts(graphql:Field 'field, int? first = 5) returns Product[]|error {
        QueryFieldClassifier classifier = new ('field, queryPlan, PRODUCTS);
        string fieldString = classifier.getFieldString();
        UnResolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();
        string queryString = wrapwithQuery("topProducts", fieldString, {"first": self.getParamAsString(first)});
        topProductsResponse response = check PRODUCTS_CLIENT->execute(queryString);
        Product[] result = response.data.topProducts;
        Resolver resolver = new (queryPlan, result, "Product", propertiesNotResolved, ["topProducts"]);
        return resolver.getResult().ensureType();
    }
}
