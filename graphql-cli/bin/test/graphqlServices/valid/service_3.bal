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

const string quote1 = "I am a high-functioning sociapath!";
const string quote2 = "I am the one who knocks!";
const string quote3 = "I can make them hurt if I want to!";
const float CONVERSION_KG_TO_LBS = 2.205;

public enum Weekday {
    SUNDAY,
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY
}

service on new graphql:Listener(9000) {
    isolated resource function get greet(string name) returns string {
        return "Hello, " + name;
    }

    isolated resource function get isLegal(int age) returns boolean {
        if age < 21 {
            return false;
        }
        return true;
    }

    isolated resource function get quote() returns string {
        return quote2;
    }

    isolated resource function get quoteById(int id = 0) returns string? {
        match id {
            0 => {
                return quote1;
            }
            1 => {
                return quote2;
            }
            2 => {
                return quote3;
            }
        }
        return;
    }

    isolated resource function get weightInPounds(float weightInKg) returns float {
        return weightInKg * CONVERSION_KG_TO_LBS;
    }

    isolated resource function get isHoliday(Weekday? weekday) returns boolean {
        if weekday == SUNDAY || weekday == SATURDAY {
            return true;
        }
        return false;
    }

    isolated resource function get getDay(boolean isHoliday) returns Weekday[] {
        if isHoliday {
            return [SUNDAY, SATURDAY];
        }
        return [MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY];
    }

    isolated resource function get sendEmail(string message) returns string {
        return message;
    }

    isolated resource function get 'type(string 'version) returns string {
        return 'version;
    }

    isolated resource function get \u{0076}ersion(string name) returns string {
        return name;
    }
}

isolated service on new graphql:Listener(9001) {
    private Person p;
    isolated function init() {
        self.p = p2.clone();
    }

    isolated resource function get person() returns Person {
        lock {
            return self.p;
        }
    }

    isolated remote function setName(string name) returns Person {
        lock {
            Person p = {name: name, age: self.p.age, address: self.p.address};
            self.p = p;
            return self.p;
        }
    }

    isolated resource function subscribe messages() returns stream<int, error?> {
        int[] intArray = [1, 2, 3, 4, 5];
        return intArray.toStream();
    }
}

service on new graphql:Listener(9002) {
    isolated resource function get greet() returns string {
        return "Hello";
    }
}

public type Address readonly & record {
    string number;
    string street;
    string city;
};

public type Person readonly & record {
    string name;
    int age;
    Address address;
};

final readonly & Person p2 = {
    name: "Walter White",
    age: 50,
    address: {
        number: "221/B",
        street: "Bakers",
        city: "london"
    }
};
