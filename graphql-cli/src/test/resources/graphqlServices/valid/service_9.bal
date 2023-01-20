/// Copyright (c) 2022 WSO2 LLC. (http://www.wso2.org). All Rights Reserved.
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

 listener graphql:Listener gql = new graphql:Listener(9090);

 service /person on gql {
     isolated resource function get name() returns string {
         return "James Moriarty";
     }

     isolated resource function get birthdate() returns string {
         return "15-05-1848";
     }

     isolated resource function get ids() returns int[] {
         return [0, 1, 2];
     }

     isolated resource function get idsWithErrors() returns (int|error)[] {
         return [0, 1, 2, error("Not Found!")];
     }

     isolated resource function get friends() returns (string|error)?[] {
         return ["walter", "jessie", error("Not Found!")];
     }
 }

 service /inputs on gql {
     isolated resource function get greet(string name) returns string {
         return "Hello, " + name;
     }

     isolated resource function get isLegal(int age) returns boolean {
         if age < 21 {
             return false;
         }
         return true;
     }
 };

graphql:Service gqlService = service object {
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
 };

 public enum Weekday {
     SUNDAY,
     MONDAY,
     TUESDAY,
     WEDNESDAY,
     THURSDAY,
     FRIDAY,
     SATURDAY
 }

 public function main() returns error? {
     check gql.attach(gqlService, "/enum");
     check gql.start();
     check gql.gracefulStop();
 }
