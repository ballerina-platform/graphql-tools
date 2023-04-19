package io.ballerina.graphql.generator.gateway.generator.common;

import graphql.language.FieldDefinition;
import graphql.schema.GraphQLAppliedDirective;
import io.ballerina.graphql.generator.gateway.exception.GatewayGenerationException;

import java.util.List;
import java.util.Map;

import static io.ballerina.graphql.generator.gateway.generator.common.CommonUtils.getClientFromFieldDefinition;
import static io.ballerina.graphql.generator.gateway.generator.common.CommonUtils.getTypeFromFieldDefinition;

/**
 * Class to hold data related to a graphql type field.
 */
public class FieldData {
    private final String fieldName;
    private final String type;
    private final String client;
    private final String typename;
    private final FieldDefinition fieldDefinition;
    private final SchemaTypes schemaTypes;


    FieldData(SchemaTypes schemaTypes, String fieldName, FieldDefinition fieldDefinition,
              List<GraphQLAppliedDirective> joinTypeDirectivesOnParent, String parentType)
            throws GatewayGenerationException {
        this.schemaTypes = schemaTypes;
        this.fieldName = fieldName;
        this.type = getTypeFromFieldDefinition(fieldDefinition);
        this.client = getClientFromFieldDefinition(fieldDefinition, joinTypeDirectivesOnParent);
        this.typename = parentType;
        this.fieldDefinition = fieldDefinition;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getType() {
        return type;
    }

    public String getClient() {
        return client;
    }

    public Map<String, String> getRequires() {
        return schemaTypes.getRequiresFromFieldDefinition(fieldDefinition, typename);
    }

    public boolean isID() {
        return this.type.equals("ID");
    }

}
