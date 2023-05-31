package io.ballerina.graphql.generator.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for representing result after comparing nodes.
 */
public class MembersEqualityResult {
    private List<String> additions;
    private List<String> violatedAdditions;
    private List<String> removals;

    public MembersEqualityResult() {
        additions = new ArrayList<>();
        violatedAdditions = new ArrayList<>();
        removals = new ArrayList<>();
    }

    public void addToAdditions(String str) {
        additions.add(str);
    }

    public void addToViolatedAdditions(String str) {
        violatedAdditions.add(str);
    }

    public void addToRemovals(String str) {
        removals.add(str);
    }

    public List<String> getViolatedAdditions() {
        return violatedAdditions;
    }

    public List<String> getRemovals() {
        return removals;
    }

    public boolean isEqual() {
        return removals.isEmpty() && additions.isEmpty();
    }
}
