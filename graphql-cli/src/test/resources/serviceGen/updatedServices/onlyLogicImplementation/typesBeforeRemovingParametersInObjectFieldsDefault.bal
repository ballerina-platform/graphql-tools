import ballerina/graphql;

type SchemaWithRemovedParametersInObjectFieldsApi service object {
    *graphql:Service;
    resource function get child(string name) returns Child?;
    resource function get adult(string name) returns Adult?;
};

public distinct service class Adult {
    resource function get name() returns string {
        return "Shawn";
    }

    resource function get age(string nic, string birthday) returns int {
        return 24;
    }
}

public distinct service class Child {
    resource function get name() returns string {
        return "John";
    }

    resource function get knowsWord(string word) returns boolean {
        if (word.length() < 5) {
            return true;
        } else {
            return false;
        }
    }

    resource function get pass(int score1, int score2, int score3 = 0) returns boolean {
        if (score1 + score2 + score3 > 200) {
            return true;
        } else {
            return false;
        }
    }
}
