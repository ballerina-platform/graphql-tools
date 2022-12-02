// Copyright (c) 2022 WSO2 LLC. (http://www.wso2.org). All Rights Reserved.
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

public service class Product {
    private final string id;

    function init(string id) {
        self.id = id;
    }

    resource function get id() returns string {
        return self.id;
    }
}

public type Review record {|
    Product product;
    int score;
    string description;
|};

table<Review> reviews = table [
    {product: new ("1"), score: 20, description: "Product 01"},
    {product: new ("2"), score: 20, description: "Product 02"}
];

service / on new graphql:Listener(9090) {
    resource function get latest() returns Review {
        return reviews.toArray().pop();
    }
}

listener graphql:Listener gql = new graphql:Listener(9091);

graphql:Service gqlService = service object {

    isolated resource function get name() returns string {
        return "Walter White";
    }
};

service / on new graphql:Listener(9092) {
    resource function get greet() returns string {
        return "hello";
    }
}

public function main() returns error? {
    check gql.attach(gqlService);
    check gql.start();
    check gql.gracefulStop();
}
