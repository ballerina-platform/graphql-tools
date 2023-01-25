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

public enum Episode {
    NEWHOPE,
    EMPIRE,
    JEDI
}

public type Review record {
    Episode episode?;
    int stars;
    string commentary?;
};

public type ReviewInput record {
    int stars;
    string commentary;
};

public type Character distinct service object {

    resource function get id() returns string;

    resource function get name() returns string;

    resource function get appearsIn() returns Episode[];
};

distinct service class Human {
    *Character;

    resource function get id() returns string {
        return "";
    }

    resource function get name() returns string {
        return "";
    }

    resource function get homePlanet() returns string? {
        return;
    }

    resource function get height() returns float? {
        return ;
    }

    resource function get mass() returns int? {
        return ;
    }

    resource function get appearsIn() returns Episode[] {
        return [JEDI];
    }
}

service /project on new graphql:Listener(9000) {

    resource function get hero(Episode? episode) returns Character {
        return new Human();
    }

    resource function get reviews(Episode episode = JEDI) returns Review?[] {
        return [];
    }

    resource function get characters(string[] idList) returns Character?[] {
        Character[] characters = [new Human()];
        return characters;
    }

    resource function get human(string id) returns Human? {
        if id.includes("human") {
            return new Human();
        }
        return;
    }

    remote function createReview(Episode episode, ReviewInput reviewInput) returns Review {
        Review review = {
            stars: reviewInput.stars
        };
        return review;
    }
}
