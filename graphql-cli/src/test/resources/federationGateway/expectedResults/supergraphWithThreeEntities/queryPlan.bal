public const string REVIEWS = "reviews";
public const string PRODUCT = "product";
public final readonly & table<QueryPlanEntry> key(typename) queryPlan = table [
    {typename: "Category", keys: {}, fields: table [
            {name: "id", 'type: "String", 'client: PRODUCT},
            {name: "title", 'type: "String", 'client: PRODUCT}
        ]},
    {typename: "Product", keys: {"product": "id", "reviews": "id"}, fields: table [
            {name: "reviews", 'type: "Review", 'client: REVIEWS},
            {name: "price", 'type: "Int", 'client: PRODUCT},
            {name: "description", 'type: "String", 'client: PRODUCT},
            {name: "title", 'type: "String", 'client: PRODUCT},
            {name: "category", 'type: "Category", 'client: PRODUCT}
        ]},
    {typename: "Review", keys: {}, fields: table [
            {name: "author", 'type: "String", 'client: REVIEWS},
            {name: "rating", 'type: "Float", 'client: REVIEWS},
            {name: "comment", 'type: "String", 'client: REVIEWS},
            {name: "id", 'type: "String", 'client: REVIEWS}
        ]}
];
