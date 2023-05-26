package io.ballerina.graphql.generator.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for representing result after comparing nodes.
 */
public class TypeEqualityResult {
    private boolean isEqual;
    private List<String> additions;
    private List<String> removals;

    public TypeEqualityResult() {
        isEqual = false;
        additions = new ArrayList<>();
        removals = new ArrayList<>();
    }

    public void addToAdditions(String str) {
        additions.add(str);
    }

    public void addToRemovals(String str) {
        removals.add(str);
    }

    public void setIsEqual(boolean isEqual) {
        this.isEqual = isEqual;
    }

    public boolean getIsEqual() {
        return isEqual;
    }

    public List<String> getAdditions() {
        return additions;
    }

    public List<String> getRemovals() {
        return removals;
    }
}
