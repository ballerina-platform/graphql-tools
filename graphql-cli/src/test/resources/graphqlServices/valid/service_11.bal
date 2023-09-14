// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.org). All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/graphql;
import ballerina/graphql.subgraph;

@subgraph:Entity {
    key: "id",
    resolveReference: resolveProduct
}
public type Product record {
    string id;
    string title;
    string description;
    int price;
    Category category;
};

public type Category record {
    string id;
    string title;
};

isolated function resolveProduct(subgraph:Representation representation) returns Product|error? {
    final string id = check representation["id"].ensureType();
    return trap products.filter(product => product.id == id).pop();
}

@subgraph:Subgraph
service /product on new graphql:Listener(4001) {
    resource function get product(string id) returns Product? {
        var product = products.filter(product => id == product.id);
        if (product.length() > 0) {
            return product[0];
        }
        return ();
    }

    resource function get products() returns Product[] {
        return products;
    }
}

final readonly & Category[] categories = [
    {
        id: "1",
        title: "Kitchen appliances"
    }
];

final readonly & Product[] products = [
    {
        id: "1",
        title: "Knife",
        description: "A knife is a tool with a cutting edge.",
        price: 100,
        category: categories[0]
    }
];
