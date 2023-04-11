public const string REVIEWS = "reviews";
public const string INVENTORY = "inventory";
public const string ACCOUNTS = "accounts";
public const string PRODUCTS = "products";
public final readonly & table<QueryPlanEntry> key(typename) queryPlan = table [
    {typename: "Product", keys: {"reviews": "upc", "inventory": "upc", "products": "upc"}, fields: table [
            {name: "reviews", 'type: "Review", 'client: REVIEWS},
            {name: "price", 'type: "Int", 'client: PRODUCTS},
            {name: "name", 'type: "String", 'client: PRODUCTS},
            {name: "weight", 'type: "Int", 'client: PRODUCTS},
            {name: "inStock", 'type: "Boolean", 'client: INVENTORY},
            {name: "shippingEstimate", 'type: "Float", 'client: INVENTORY, requires: [{clientName: PRODUCTS, fieldString: "price weight dimensions {length width}"}]},
            {name: "dimensions", 'type: "ProductDimension", 'client: PRODUCTS}
        ]},
    {typename: "ProductDimension", keys: {"inventory": "upc", "products": "upc"}, fields: table [
            {name: "width", 'type: "Int", 'client: PRODUCTS},
            {name: "length", 'type: "Int", 'client: PRODUCTS},
            {name: "height", 'type: "Int", 'client: PRODUCTS}
        ]},
    {typename: "Review", keys: {"reviews": "id"}, fields: table [
            {name: "product", 'type: "Product", 'client: REVIEWS},
            {name: "author", 'type: "User", 'client: REVIEWS},
            {name: "body", 'type: "String", 'client: REVIEWS}
        ]},
    {typename: "User", keys: {"reviews": "id", "accounts": "id"}, fields: table [
            {name: "reviews", 'type: "Review", 'client: REVIEWS},
            {name: "name", 'type: "String", 'client: ACCOUNTS},
            {name: "username", 'type: "String", 'client: ACCOUNTS}
        ]}
];
