import ballerina/graphql;

final graphql:Client REVIEWS_CLIENT = check new graphql:Client("http://localhost:4002");
final graphql:Client INVENTORY_CLIENT = check new graphql:Client("http://localhost:4004");
final graphql:Client ACCOUNTS_CLIENT = check new graphql:Client("http://localhost:4001");
final graphql:Client PRODUCTS_CLIENT = check new graphql:Client("http://localhost:4003");

isolated function getClient(string clientName) returns graphql:Client {
    if (clientName == "reviews") {
        return REVIEWS_CLIENT;
    } else if (clientName == "inventory") {
        return INVENTORY_CLIENT;
    } else if (clientName == "accounts") {
        return ACCOUNTS_CLIENT;
    } else if (clientName == "products") {
        return PRODUCTS_CLIENT;
    } else {
        panic error("Client not found");
    }
};

configurable int PORT = 9000;

isolated service on new graphql:Listener(PORT) {
    isolated resource function get me(graphql:Field 'field) returns User|error {
        QueryFieldClassifier classifier = new ('field, queryPlan, ACCOUNTS);
        string fieldString = classifier.getFieldString();
        UnResolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();
        string queryString = wrapwithQuery("me", fieldString);
        meResponse response = check ACCOUNTS_CLIENT->execute(queryString);
        User result = response.data.me;
        Resolver resolver = new (queryPlan, result, "User", propertiesNotResolved, ["me"]);
        return resolver.getResult().ensureType();
    };
    isolated resource function get topProducts(graphql:Field 'field, int? first = 5) returns Product[]|error {
        QueryFieldClassifier classifier = new ('field, queryPlan, PRODUCTS);
        string fieldString = classifier.getFieldString();
        UnResolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();
        string queryString = wrapwithQuery("topProducts", fieldString, {"first": first.toString()});
        topProductsResponse response = check PRODUCTS_CLIENT->execute(queryString);
        Product[] result = response.data.topProducts;
        Resolver resolver = new (queryPlan, result, "Product", propertiesNotResolved, ["topProducts"]);
        return resolver.getResult().ensureType();
    };
}
