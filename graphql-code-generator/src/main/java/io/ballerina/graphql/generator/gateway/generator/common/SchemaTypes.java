package io.ballerina.graphql.generator.gateway.generator.common;

import graphql.language.Argument;
import graphql.language.Directive;
import graphql.language.Document;
import graphql.language.Field;
import graphql.language.FieldDefinition;
import graphql.language.OperationDefinition;
import graphql.language.Selection;
import graphql.language.StringValue;
import graphql.parser.Parser;
import graphql.schema.GraphQLAppliedDirective;
import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.generator.gateway.exception.GatewayGenerationException;
import io.ballerina.graphql.generator.utils.graphql.SpecReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class to hold data related to graphql types in a given graphql schema.
 */
public class SchemaTypes {
    private final Map<String, List<FieldData>> fieldDataMap;

    public SchemaTypes(GraphQLSchema graphQLSchema) throws GatewayGenerationException {
        List<String> names = CommonUtils.getCustomDefinedObjectTypeNames(graphQLSchema);

        this.fieldDataMap = new HashMap<>();
        for (String name : names) {
            fieldDataMap.put(name, getFieldsOfType(name, graphQLSchema));
        }
    }

    /**
     * Return the list of fields of the given type.
     *
     * @param name Type name
     * @return List of fields
     * */
    public List<FieldData> getFieldsOfType(String name) {
        return fieldDataMap.get(name);
    }

    private Map<String, String> getClassifiedRequiresString(String requiresString,
                                                            String parentType) {
        Map<String, List<Selection>> clientToFieldMap = new HashMap<>();
        Document document = Parser.parse("query{ Document {" + requiresString + "} }");
        List<Selection> requires =
                ((Field) ((OperationDefinition) document.getDefinitions().get(0)).getSelectionSet().
                        getSelections().get(0)).getSelectionSet().getSelections();
        List<FieldData> fields = this.fieldDataMap.get(parentType);
        for (Selection selection : requires) {
            if (selection instanceof Field) {
                String field = ((Field) selection).getName();
                for (FieldData fieldData : fields) {
                    if (fieldData.getFieldName().equals(field)) {
                        clientToFieldMap.computeIfAbsent(fieldData.getClient(), k -> new ArrayList<>()).add(selection);
                    }
                    // TODO: if the field is an selection set handle it. Currently checking only parent field
                    //  resolving client
                }
            }
        }

        Map<String, String> classifiedRequires = new HashMap<>();
        for (Map.Entry<String, List<Selection>> entry : clientToFieldMap.entrySet()) {
            classifiedRequires.put(entry.getKey(), getFieldAsString(entry.getValue()));
        }
        return classifiedRequires;
    }

    private static String getFieldAsString(List<Selection> fields) {
        List<String> fieldStrings = new ArrayList<>();
        for (Selection selection : fields) {

            if (selection instanceof Field) {
                if (((Field) selection).getSelectionSet() != null) {
                    fieldStrings.add(((Field) selection).getName() + " {" +
                            getFieldAsString(((Field) selection).getSelectionSet().getSelections()) + "}");
                } else {
                    fieldStrings.add(((Field) selection).getName());
                }
            }
        }

        return String.join(" ", fieldStrings);
    }

    /**
     * Return the list of fields of the given type.
     *
     * @param typeName      Type name
     * @param graphQLSchema GraphQL schema
     * @return List of fields
     */
    private List<FieldData> getFieldsOfType(String typeName, GraphQLSchema graphQLSchema)
            throws GatewayGenerationException {
        List<FieldData> fields = new ArrayList<>();

        List<GraphQLAppliedDirective> joinTypeDirectives =
                SpecReader.getObjectTypeDirectives(graphQLSchema, typeName).stream().filter(
                        directive -> directive.getName().equals("join__type")
                ).collect(Collectors.toList());
        for (Map.Entry<String, FieldDefinition> entry :
                SpecReader.getObjectTypeFieldDefinitionMap(graphQLSchema, typeName).entrySet()) {
            FieldData field = new FieldData(this,
                    entry.getKey(), entry.getValue(), joinTypeDirectives, typeName);
            if (field.getClient() != null) {
                fields.add(field);
            }
        }
        return fields;
    }

    Map<String, String> getRequiresFromFieldDefinition(FieldDefinition definition,
                                                       String parentType) {
        for (Directive directive : definition.getDirectives()) {
            if (directive.getName().equals("join__field")) {
                for (Argument argument : directive.getArguments()) {
                    if (argument.getName().equals("requires")) {
                        String requiresString = ((StringValue) argument.getValue()).getValue();
                        return getClassifiedRequiresString(requiresString, parentType);
                    }
                }
            }
        }
        return null;
    }
}
