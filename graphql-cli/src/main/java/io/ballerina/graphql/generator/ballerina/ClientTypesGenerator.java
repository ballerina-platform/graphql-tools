package io.ballerina.graphql.generator.ballerina;

import graphql.schema.GraphQLSchema;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.graphql.exception.ClientTypesGenerationException;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EOF_TOKEN;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.EMPTY_STRING;

/**
 * This class is used to generate the types file content.
 */
public class ClientTypesGenerator extends TypesGenerator {
    public static ClientTypesGenerator clientTypesGenerator = null;

    public static ClientTypesGenerator getInstance() {
        if (clientTypesGenerator == null) {
            clientTypesGenerator = new ClientTypesGenerator();
        }
        return clientTypesGenerator;
    }

    /**
     * Generates the types file content.
     *
     * @param schema                        the object instance of the GraphQL schema (SDL)
     * @param documents                     the list of documents of a given GraphQL project
     * @return                              the types file content
     * @throws ClientTypesGenerationException     when an error occurs during type generation
     */
    public String generateSrc(GraphQLSchema schema, List<String> documents) throws ClientTypesGenerationException {
        try {
            String generatedSyntaxTree = Formatter.format(this.generateSyntaxTree(schema, documents)).toString();
            return Formatter.format(generatedSyntaxTree);
        } catch (FormatterException | IOException e) {
            throw new ClientTypesGenerationException(e.getMessage());
        }
    }

    /**
     * Generates the types syntax tree.
     *
     * @param schema            the object instance of the GraphQL schema (SDL)
     * @param documents         the list of documents of a given GraphQL project
     * @return                  Syntax tree for the types.bal
     * @throws IOException      If an I/O error occurs
     */
    public SyntaxTree generateSyntaxTree(GraphQLSchema schema, List<String> documents) throws IOException {
        List<TypeDefinitionNode> typeDefinitionNodeList = new LinkedList<>();
        NodeList<ImportDeclarationNode> importsList = createEmptyNodeList();

        addInputRecords(schema, typeDefinitionNodeList);
        addQueryResponseRecords(schema, documents, typeDefinitionNodeList);

        NodeList<ModuleMemberDeclarationNode> members =  createNodeList(typeDefinitionNodeList.toArray(
                new TypeDefinitionNode[typeDefinitionNodeList.size()]));
        ModulePartNode modulePartNode = createModulePartNode(
                importsList,
                members,
                createToken(EOF_TOKEN));

        TextDocument textDocument = TextDocuments.from(EMPTY_STRING);
        SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
        return syntaxTree.modifyWith(modulePartNode);
    }
}
