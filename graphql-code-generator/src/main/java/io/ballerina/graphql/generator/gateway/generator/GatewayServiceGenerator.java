package io.ballerina.graphql.generator.gateway.generator;

import graphql.language.BooleanValue;
import graphql.language.EnumValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.GraphQLAppliedDirective;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLSchemaElement;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeParser;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.graphql.generator.gateway.GraphqlGatewayProject;
import io.ballerina.graphql.generator.gateway.exception.GatewayGenerationException;
import io.ballerina.graphql.generator.gateway.exception.GatewayServiceGenerationException;
import io.ballerina.graphql.generator.gateway.generator.common.CommonUtils;
import io.ballerina.graphql.generator.gateway.generator.common.JoinGraph;
import io.ballerina.graphql.generator.utils.graphql.Utils;
import io.ballerina.graphql.generator.utils.model.FieldType;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.ballerinalang.formatter.core.Formatter;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EOF_TOKEN;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.EMPTY_STRING;
import static io.ballerina.graphql.generator.gateway.generator.common.CommonUtils.getJoinGraphs;
import static io.ballerina.graphql.generator.gateway.generator.common.CommonUtils.getResourceTemplateFilePath;
import static io.ballerina.graphql.generator.gateway.generator.common.Constants.BALLERINA_GRAPHQL_IMPORT_STATEMENT;
import static io.ballerina.graphql.generator.gateway.generator.common.Constants.BALLERINA_LOG_IMPORT_STATEMENT;
import static io.ballerina.graphql.generator.gateway.generator.common.Constants.BASIC_RESPONSE_TYPE_PLACEHOLDER;
import static io.ballerina.graphql.generator.gateway.generator.common.Constants.CLIENT_NAME_PLACEHOLDER;
import static io.ballerina.graphql.generator.gateway.generator.common.Constants.CLIENT_NAME_VALUE_PLACEHOLDER;
import static io.ballerina.graphql.generator.gateway.generator.common.Constants.CONFIGURABLE_PORT_STATEMENT;
import static io.ballerina.graphql.generator.gateway.generator.common.Constants.FUNCTION_PARAM_PLACEHOLDER;
import static io.ballerina.graphql.generator.gateway.generator.common.Constants.GET_CLIENT_FUNCTION_TEMPLATE_FILE;
import static io.ballerina.graphql.generator.gateway.generator.common.Constants.GRAPHQL_CLIENT_DECLARATION_STATEMENT;
import static io.ballerina.graphql.generator.gateway.generator.common.Constants.MATCH_CLIENT_STATEMENTS_PLACEHOLDER;
import static io.ballerina.graphql.generator.gateway.generator.common.Constants.MATCH_CLIENT_STATEMENT_TEMPLATE;
import static io.ballerina.graphql.generator.gateway.generator.common.Constants.QUERY_ARGS_PLACEHOLDER;
import static io.ballerina.graphql.generator.gateway.generator.common.Constants.QUERY_PLACEHOLDER;
import static io.ballerina.graphql.generator.gateway.generator.common.Constants.RESOURCE_FUNCTIONS_PLACEHOLDER;
import static io.ballerina.graphql.generator.gateway.generator.common.Constants.RESOURCE_FUNCTION_TEMPLATE_FILE;
import static io.ballerina.graphql.generator.gateway.generator.common.Constants.RESPONSE_TYPE_PLACEHOLDER;
import static io.ballerina.graphql.generator.gateway.generator.common.Constants.SERVICE_DECLARATION_TEMPLATE_FILE;
import static io.ballerina.graphql.generator.gateway.generator.common.Constants.URL_PLACEHOLDER;

/**
 * Class to generate service code for the gateway.
 */
public class GatewayServiceGenerator {
    private final GraphqlGatewayProject project;
    private final Map<String, JoinGraph> joinGraphs;

    public GatewayServiceGenerator(GraphqlGatewayProject project) throws IOException {
        this.project = project;
        joinGraphs = getJoinGraphs(project.getGraphQLSchema());
    }

    public String generateSrc() throws GatewayServiceGenerationException {
        try {
            SyntaxTree syntaxTree = generateSyntaxTree();
            return Formatter.format(syntaxTree).toString();
        } catch (Exception e) {
            throw new GatewayServiceGenerationException("Error while generating the gateway services", e);
        }
    }

    private SyntaxTree generateSyntaxTree() throws GatewayGenerationException, IOException {
        NodeList<ImportDeclarationNode> importsList = createNodeList(
                NodeParser.parseImportDeclaration(BALLERINA_GRAPHQL_IMPORT_STATEMENT),
                NodeParser.parseImportDeclaration(BALLERINA_LOG_IMPORT_STATEMENT)
        );

        List<ModuleMemberDeclarationNode> nodes = new ArrayList<>(getClientDeclarations());
        nodes.add(getGetClientFunction());
        nodes.add(NodeParser.parseModuleMemberDeclaration(CONFIGURABLE_PORT_STATEMENT));
        nodes.add(getServiceDeclaration());

        NodeList<ModuleMemberDeclarationNode> members = createNodeList(
                nodes.toArray(
                        new ModuleMemberDeclarationNode[0])
        );

        ModulePartNode modulePartNode = createModulePartNode(
                importsList,
                members,
                createToken(EOF_TOKEN));

        TextDocument textDocument = TextDocuments.from(EMPTY_STRING);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }

    private ModuleMemberDeclarationNode getServiceDeclaration() throws GatewayGenerationException, IOException {
        String resourceFunctions = String.join(System.lineSeparator(), getResourceFunctions());
        String serviceTemplate = Files.readString(getResourceTemplateFilePath(project.getTempDir(),
                SERVICE_DECLARATION_TEMPLATE_FILE)).replaceAll(RESOURCE_FUNCTIONS_PLACEHOLDER, resourceFunctions);
        return NodeParser.parseModuleMemberDeclaration(serviceTemplate);
    }

    private List<String> getResourceFunctions() throws GatewayGenerationException, IOException {
        List<String> resourceFunctions = new ArrayList<>();
        for (GraphQLSchemaElement graphQLObjectType : CommonUtils.getQueryTypes(project.getGraphQLSchema())) {
            resourceFunctions.add(getResourceFunction(graphQLObjectType));
        }
        return resourceFunctions;
    }

    private String getResourceFunction(GraphQLSchemaElement graphQLSchemaElement)
            throws IOException, GatewayGenerationException {
        return Files.readString(getResourceTemplateFilePath(project.getTempDir(),
                        RESOURCE_FUNCTION_TEMPLATE_FILE)).replaceAll(QUERY_PLACEHOLDER,
                        ((GraphQLFieldDefinition) graphQLSchemaElement).getName())
                .replaceAll(FUNCTION_PARAM_PLACEHOLDER,
                        getArgumentString(graphQLSchemaElement))
                .replaceAll(RESPONSE_TYPE_PLACEHOLDER,
                        CommonUtils.getTypeNameFromGraphQLType(
                                ((GraphQLFieldDefinition) graphQLSchemaElement).getType()))
                .replaceAll(BASIC_RESPONSE_TYPE_PLACEHOLDER,
                        CommonUtils.getBasicTypeNameFromGraphQLType(
                                ((GraphQLFieldDefinition) graphQLSchemaElement).getType()))
                .replaceAll(CLIENT_NAME_PLACEHOLDER,
                        getClientNameFromFieldDefinition((GraphQLFieldDefinition) graphQLSchemaElement))
                .replaceAll(QUERY_ARGS_PLACEHOLDER, getQueryArguments(graphQLSchemaElement));
    }

    private ModuleMemberDeclarationNode getGetClientFunction()
            throws IOException {
        List<String> matchClientCases = new ArrayList<>();
        for (Map.Entry<String, JoinGraph> entry : joinGraphs.entrySet()) {
            matchClientCases.add(
                    MATCH_CLIENT_STATEMENT_TEMPLATE
                            .replace(CLIENT_NAME_PLACEHOLDER, entry.getKey())
                            .replace(CLIENT_NAME_VALUE_PLACEHOLDER, entry.getValue().getName())
            );
        }
        String functionTemplate = Files.readString(getResourceTemplateFilePath(project.getTempDir(),
                GET_CLIENT_FUNCTION_TEMPLATE_FILE));
        functionTemplate = functionTemplate.replaceAll(
                MATCH_CLIENT_STATEMENTS_PLACEHOLDER,
                String.join(System.lineSeparator(), matchClientCases)
        );

        return NodeParser.parseModuleMemberDeclaration(functionTemplate);
    }

    private List<ModuleMemberDeclarationNode> getClientDeclarations() {
        List<ModuleMemberDeclarationNode> nodes = new ArrayList<>();
        for (Map.Entry<String, JoinGraph> entry : joinGraphs.entrySet()) {
            String key = entry.getKey();
            JoinGraph value = entry.getValue();
            nodes.add(
                    NodeParser.parseModuleMemberDeclaration(
                            GRAPHQL_CLIENT_DECLARATION_STATEMENT.replace(CLIENT_NAME_PLACEHOLDER, key)
                                    .replace(URL_PLACEHOLDER, value.getUrl())
                    )
            );
        }
        return nodes;
    }

    private String getClientNameFromFieldDefinition(GraphQLFieldDefinition graphQLFieldDefinition) {
        for (GraphQLAppliedDirective directive : graphQLFieldDefinition.getAppliedDirectives()) {
            if (directive.getName().equals("join__field")) {
                return ((EnumValue) Objects.requireNonNull(
                        directive.getArgument("graph").getArgumentValue().getValue())).getName();
            }
        }
        throw new GatewayServiceGenerationException("No client name found: " + graphQLFieldDefinition.getName());
    }

    private String getValue(Value value) {
        if (value instanceof IntValue) {
            return ((IntValue) value).getValue().toString();
        } else if (value instanceof StringValue) {
            return "\"" + ((StringValue) value).getValue() + "\"";
        } else if (value instanceof BooleanValue) {
            return ((BooleanValue) value).isValue() ? "true" : "false";
        } else if (value instanceof FloatValue) {
            return ((FloatValue) value).getValue().toString();
        } else {
            throw new GatewayServiceGenerationException("Unsupported value: " + value);
        }
    }

    private String getArgumentString(GraphQLSchemaElement graphQLObjectType) {
        StringBuilder arguments = new StringBuilder();
        for (GraphQLArgument argument : ((GraphQLFieldDefinition) graphQLObjectType).getArguments()) {
            arguments.append(", ");
            FieldType fieldType = Utils.getFieldType(project.getGraphQLSchema(),
                    Objects.requireNonNull(argument.getDefinition()).getType());
            if (argument.getDefinition().getDefaultValue() != null) {
                arguments.append(fieldType.getName()).append(fieldType.getTokens()).append(" ")
                        .append(argument.getName()).append(" = ")
                        .append(getValue(argument.getDefinition().getDefaultValue()));
            } else {
                arguments.append(fieldType.getName()).append(fieldType.getTokens()).append(" ")
                        .append(argument.getName());
            }
        }
        return arguments.toString();
    }

    private String getQueryArguments(GraphQLSchemaElement graphQLObjectType) {
        StringBuilder argumentString = new StringBuilder();
        List<GraphQLArgument> arguments = ((GraphQLFieldDefinition) graphQLObjectType).getArguments();
        if (arguments.size() > 0) {
            argumentString.append(", ");
            argumentString.append("{");
            argumentString.append(getQueryArgumentList(arguments));
            argumentString.append("}");
        }
        return argumentString.toString();
    }

    private String getQueryArgumentList(List<GraphQLArgument> arguments) {
        StringBuilder argumentList = new StringBuilder();
        int size = arguments.size();
        int count = 0;
        for (GraphQLArgument argument : arguments) {
            argumentList.append("\"").append(argument.getName()).append("\": getParamAsString(")
                    .append(argument.getName()).append(")");

            if (size < ++count) {
                argumentList.append(", ");
            }
        }
        return argumentList.toString();
    }
}
