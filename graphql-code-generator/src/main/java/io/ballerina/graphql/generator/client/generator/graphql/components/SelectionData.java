package io.ballerina.graphql.generator.client.generator.graphql.components;

import graphql.schema.GraphQLSchema;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.graphql.generator.client.generator.graphql.QueryReader;
import io.ballerina.graphql.generator.client.generator.model.FieldType;

import java.util.List;
import java.util.Map;

/**
 * Model class representing information about a Selection.
 */
public class SelectionData {
    private String selectionType;
    private Map<String, FieldType> fieldsOfSelectionType;
    private GraphQLSchema schema;
    private QueryReader queryReader;
    private List<Node> fieldsOfInlineRecord;
    private List<TypeDefinitionNode> typeDefinitionNodeList;
    private Map<String, String> fragmentRecordsMap;

    public SelectionData(String selectionType, Map<String, FieldType> fieldsOfSelectionType, GraphQLSchema schema,
                         QueryReader queryReader, List<Node> fieldsOfInlineRecord, List<TypeDefinitionNode>
                                 typeDefinitionNodeList, Map<String, String> fragmentRecordsMap) {
        this.selectionType = selectionType;
        this.fieldsOfSelectionType = fieldsOfSelectionType;
        this.schema = schema;
        this.queryReader = queryReader;
        this.fieldsOfInlineRecord = fieldsOfInlineRecord;
        this.typeDefinitionNodeList = typeDefinitionNodeList;
        this.fragmentRecordsMap = fragmentRecordsMap;
    }

    public String getSelectionType() {
        return selectionType;
    }

    public Map<String, FieldType> getFieldsOfSelectionType() {
        return fieldsOfSelectionType;
    }

    public GraphQLSchema getSchema() {
        return schema;
    }

    public QueryReader getQueryReader() {
        return queryReader;
    }

    public List<Node> getFieldsOfInlineRecord() {
        return fieldsOfInlineRecord;
    }

    public List<TypeDefinitionNode> getTypeDefinitionNodeList() {
        return typeDefinitionNodeList;
    }

    public Map<String, String> getFragmentRecordsMap() {
        return fragmentRecordsMap;
    }
}
