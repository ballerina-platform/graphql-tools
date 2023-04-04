public type Product record {|
    Review?[]? reviews?;
    int? price?;
    string? name?;
    string upc?;
    int? weight?;
    boolean? inStock?;
    float? shippingEstimate?;
    ProductDimension? dimensions?;
|};

public type ProductDimension record {|
    int? width?;
    int? length?;
    string upc?;
    int? height?;
|};

public type Review record {|
    Product? product?;
    User? author?;
    string id?;
    string? body?;
|};

public type User record {|
    Review?[]? reviews?;
    string? name?;
    string id?;
    string? username?;
|};

public type meResponse record {
    record {|User me;|} data;
};

public type topProductsResponse record {
    record {|[Product] topProducts;|} data;
};