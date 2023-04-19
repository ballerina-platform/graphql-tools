import ballerina/graphql;
import ballerina/log;

final graphql:Client REVIEWS_CLIENT = check new graphql:Client("http://localhost:4002");
final graphql:Client PRODUCT_CLIENT = check new graphql:Client("http://localhost:4001");

isolated function getClient(string clientName) returns graphql:Client {
    match clientName {
        "reviews" => {
            return REVIEWS_CLIENT;
        }
        "product" => {
            return PRODUCT_CLIENT;
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

    isolated resource function get product(graphql:Field 'field, string id) returns Product|error {
        QueryFieldClassifier classifier = new ('field, queryPlan, PRODUCT);
        string fieldString = classifier.getFieldString();
        UnResolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();
        string queryString = wrapwithQuery("product", fieldString, {"id": self.getParamAsString(id)});
        productResponse response = check PRODUCT_CLIENT->execute(queryString);
        Product result = response.data.product;
        Resolver resolver = new (queryPlan, result, "Product", propertiesNotResolved, ["product"]);
        return resolver.getResult().ensureType();
    }
    isolated resource function get products(graphql:Field 'field) returns Product[]|error {
        QueryFieldClassifier classifier = new ('field, queryPlan, PRODUCT);
        string fieldString = classifier.getFieldString();
        UnResolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();
        string queryString = wrapwithQuery("products", fieldString);
        productsResponse response = check PRODUCT_CLIENT->execute(queryString);
        Product[] result = response.data.products;
        Resolver resolver = new (queryPlan, result, "Product", propertiesNotResolved, ["products"]);
        return resolver.getResult().ensureType();
    }
    isolated resource function get reviews(graphql:Field 'field, string productId) returns Review[]|error {
        QueryFieldClassifier classifier = new ('field, queryPlan, REVIEWS);
        string fieldString = classifier.getFieldString();
        UnResolvableField[] propertiesNotResolved = classifier.getUnresolvableFields();
        string queryString = wrapwithQuery("reviews", fieldString, {"productId": self.getParamAsString(productId)});
        reviewsResponse response = check REVIEWS_CLIENT->execute(queryString);
        Review[] result = response.data.reviews;
        Resolver resolver = new (queryPlan, result, "Review", propertiesNotResolved, ["reviews"]);
        return resolver.getResult().ensureType();
    }
}
