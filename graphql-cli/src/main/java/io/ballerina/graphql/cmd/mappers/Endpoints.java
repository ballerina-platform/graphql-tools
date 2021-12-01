package io.ballerina.graphql.cmd.mappers;

/**
 * POJO class representing endpoints of a project in GraphQL config file.
 */
public class Endpoints {
    private Default defaultName;

    public Default getDefaultName() {
        return defaultName;
    }

    public void setDefaultName(Default defaultName) {
        this.defaultName = defaultName;
    }
}
