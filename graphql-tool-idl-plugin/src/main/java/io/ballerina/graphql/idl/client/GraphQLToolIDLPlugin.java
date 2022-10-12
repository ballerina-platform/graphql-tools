package io.ballerina.graphql.idl.client;

import graphql.language.Document;
import graphql.schema.GraphQLSchema;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionSignatureNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDescriptorNode;
import io.ballerina.graphql.cmd.GraphqlProject;
import io.ballerina.graphql.cmd.pojo.Config;
import io.ballerina.graphql.cmd.pojo.Extension;
import io.ballerina.graphql.exception.ClientGenerationException;
import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.ConfigTypesGernerationException;
import io.ballerina.graphql.exception.GenerationException;
import io.ballerina.graphql.exception.ParseException;
import io.ballerina.graphql.exception.TypesGenerationException;
import io.ballerina.graphql.exception.UtilsGenerationException;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.CodeGeneratorConstants;
import io.ballerina.graphql.generator.CodeGeneratorUtils;
import io.ballerina.graphql.generator.ballerina.AuthConfigGenerator;
import io.ballerina.graphql.generator.ballerina.ConfigTypesGenerator;
import io.ballerina.graphql.generator.ballerina.FunctionBodyGenerator;
import io.ballerina.graphql.generator.ballerina.FunctionSignatureGenerator;
import io.ballerina.graphql.generator.ballerina.TypesGenerator;
import io.ballerina.graphql.generator.ballerina.UtilsGenerator;
import io.ballerina.graphql.generator.graphql.QueryReader;
import io.ballerina.graphql.generator.graphql.components.ExtendedOperationDefinition;
import io.ballerina.graphql.generator.model.AuthConfig;
import io.ballerina.graphql.generator.model.SrcFilePojo;
import io.ballerina.graphql.validator.QueryValidator;
import io.ballerina.graphql.validator.SDLValidator;
import io.ballerina.projects.DocumentConfig;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.ModuleConfig;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.plugins.IDLClientGenerator;
import io.ballerina.projects.plugins.IDLGeneratorPlugin;
import io.ballerina.projects.plugins.IDLPluginContext;
import io.ballerina.projects.plugins.IDLSourceGeneratorContext;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createEmptyNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createIdentifierToken;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createNodeList;
import static io.ballerina.compiler.syntax.tree.AbstractNodeFactory.createToken;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createClassDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createFunctionDefinitionNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createIntersectionTypeDescriptorNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createMetadataNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createModulePartNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createObjectFieldNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createQualifiedNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createSimpleNameReferenceNode;
import static io.ballerina.compiler.syntax.tree.NodeFactory.createTypeReferenceTypeDescNode;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.BITWISE_AND_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLASS_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLIENT_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.CLOSE_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.COLON_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.EOF_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FINAL_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.FUNCTION_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.ISOLATED_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.OPEN_BRACE_TOKEN;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.PUBLIC_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.READONLY_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.REMOTE_KEYWORD;
import static io.ballerina.compiler.syntax.tree.SyntaxKind.SEMICOLON_TOKEN;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.API_KEYS_CONFIG_PARAM_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.API_KEYS_CONFIG_TYPE_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.CLIENT_FILE_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.CONFIG_TYPES_FILE_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.EMPTY_STRING;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.GRAPHQL;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.GRAPHQL_CLIENT;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.IDL_MODULE_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.INIT;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.TYPES_FILE_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.UTILS_FILE_NAME;

/**
 * IDL client generation class.
 */
public class GraphQLToolIDLPlugin extends IDLGeneratorPlugin {

    @Override
    public void init(IDLPluginContext idlPluginContext) {
        idlPluginContext.addCodeGenerator(new GraphQLClientGenerator());
    }

    private static class GraphQLClientGenerator extends IDLClientGenerator {

        @Override
        public boolean canHandle(IDLSourceGeneratorContext idlSourceGeneratorContext) {
            // check the validity of the yml file. For valid graphql file. Use configValidator API
            try {
                Config config = Utils.readConfig(idlSourceGeneratorContext.resourcePath().toString());
                List<GraphqlProject> projects = Utils.populateProjects(config, EMPTY_STRING);
                if (projects.size() > 1) {
                    // Give an error message mentioning that it includes multiple clients
                    return false;
                }
                SDLValidator.getInstance().validate(projects.get(0));
                QueryValidator.getInstance().validate(projects.get(0));
            } catch (ParseException | CmdException | ValidationException | IOException e) {
                return false;
            }
            return true;
        }

        @Override
        public void perform(IDLSourceGeneratorContext idlSourceGeneratorContext) {
            String targetOutputPath = EMPTY_STRING;
            String moduleName = IDL_MODULE_NAME;
            try {
                Config config = Utils.readConfig(idlSourceGeneratorContext.resourcePath().toString());
                List<GraphqlProject> projects = Utils.populateProjects(config, targetOutputPath);
                SDLValidator.getInstance().validate(projects.get(0));
                List<SrcFilePojo> genSrcFiles = generateBalSources(projects.get(0));
                ModuleId moduleId = ModuleId.create(moduleName, idlSourceGeneratorContext.currentPackage().packageId());
                List<DocumentConfig> documents = new ArrayList<>();

                genSrcFiles.stream().forEach(genSrcFile -> {
                    DocumentId documentId = DocumentId.create(genSrcFile.getFileName(), moduleId);
                    DocumentConfig documentConfig = DocumentConfig.from(
                            documentId, genSrcFile.getContent(), genSrcFile.getFileName());
                    documents.add(documentConfig);
                });
                ModuleDescriptor moduleDescriptor = ModuleDescriptor.from(
                        ModuleName.from(idlSourceGeneratorContext.currentPackage().packageName(), moduleName),
                        idlSourceGeneratorContext.currentPackage().descriptor());
                ModuleConfig moduleConfig = ModuleConfig.from(moduleId, moduleDescriptor, documents,
                        Collections.emptyList(), null, new ArrayList<>());
                idlSourceGeneratorContext.addClient(moduleConfig, NodeFactory.createEmptyNodeList());
            } catch (GenerationException | IOException | ParseException | CmdException | ValidationException e) {
                Constants.DiagnosticMessages error = Constants.DiagnosticMessages.ERROR_WHILE_GENERATING_CLIENT;
                Utils.reportDiagnostic(idlSourceGeneratorContext, error,
                        idlSourceGeneratorContext.clientNode().location());
            }
        }

        public List<SrcFilePojo> generateBalSources(GraphqlProject project) throws ClientGenerationException,
                UtilsGenerationException, TypesGenerationException, IOException, ConfigTypesGernerationException {
            String projectName = project.getName();
            Extension extensions = project.getExtensions();
            List<String> documents = project.getDocuments();
            GraphQLSchema schema = project.getGraphQLSchema(); // Use get schema and remove validation which populates

            AuthConfig authConfig = new AuthConfig();
            AuthConfigGenerator.getInstance().populateAuthConfigTypes(extensions, authConfig);
            AuthConfigGenerator.getInstance().populateApiHeaders(extensions, authConfig);

            List<SrcFilePojo> sourceFiles = new ArrayList<>();
            generateClients(projectName, documents, schema, authConfig, sourceFiles);
            generateUtils(projectName, authConfig, sourceFiles);
            generateTypes(projectName, documents, schema, sourceFiles);
            generateConfigTypes(projectName, authConfig, sourceFiles);

            return sourceFiles;
        }

        /**
         * Generates the Ballerina clients source codes for a given GraphQL project.
         */
        public void generateClients(String projectName, List<String> documents, GraphQLSchema schema,
                                    AuthConfig authConfig, List<SrcFilePojo> sourceFiles)
                throws ClientGenerationException, IOException {
            String clientSrc = generateSrc(documents, schema, authConfig);
            sourceFiles.add(new SrcFilePojo(SrcFilePojo.GenFileType.GEN_SRC, projectName,
                    CLIENT_FILE_NAME, clientSrc));
        }
        /**
         * Generates the Ballerina types source codes for a given GraphQL project.
         */
        public void generateTypes(String projectName, List<String> documents, GraphQLSchema schema,
                                  List<SrcFilePojo> sourceFiles) throws TypesGenerationException {
            String typesFileContent = TypesGenerator.getInstance().generateSrc(schema, documents);
            sourceFiles.add(new SrcFilePojo(SrcFilePojo.GenFileType.MODEL_SRC, projectName,
                    TYPES_FILE_NAME, typesFileContent));
        }

        /**
         * Generates the Ballerina utils source codes for a given GraphQL project.
         */
        public void generateUtils(String projectName, AuthConfig authConfig, List<SrcFilePojo> sourceFiles)
                throws UtilsGenerationException {
            String utilSrc = UtilsGenerator.getInstance().generateSrc(authConfig);
            sourceFiles.add(new SrcFilePojo(SrcFilePojo.GenFileType.UTIL_SRC, projectName,
                    UTILS_FILE_NAME, utilSrc));
        }

        /**
         * Generates the Ballerina config types source codes for a given GraphQL project.
         */
        private void generateConfigTypes(String projectName, AuthConfig authConfig, List<SrcFilePojo> sourceFiles)
                throws ConfigTypesGernerationException {
            String configTypesSrc = ConfigTypesGenerator.getInstance().generateSrc(authConfig);
            sourceFiles.add(new SrcFilePojo(SrcFilePojo.GenFileType.CONFIG_SRC, projectName,
                    CONFIG_TYPES_FILE_NAME, configTypesSrc));
        }

        /**
         * Generates the client file content.
         */
        public String generateSrc(List<String> queryDocuments, GraphQLSchema graphQLSchema,
                                  AuthConfig authConfig) throws ClientGenerationException {
            try {
                return Formatter.format(generateSyntaxTree(queryDocuments, graphQLSchema, authConfig)).toString();
            } catch (FormatterException | IOException e) {
                throw new ClientGenerationException(e.getMessage());
            }
        }

        /**
         * Generates the client syntax tree.
         */
        private SyntaxTree generateSyntaxTree(List<String> queryDocuments,
                                              GraphQLSchema graphQLSchema, AuthConfig authConfig) throws IOException {
            // Generate imports
            NodeList<ImportDeclarationNode> imports = generateImports();
            // Generate auth config records & client class
            NodeList<ModuleMemberDeclarationNode> members =
                    generateMembers(queryDocuments, graphQLSchema, authConfig);

            ModulePartNode modulePartNode = createModulePartNode(imports, members, createToken(EOF_TOKEN));

            TextDocument textDocument = TextDocuments.from(EMPTY_STRING);
            SyntaxTree syntaxTree = SyntaxTree.from(textDocument);
            return syntaxTree.modifyWith(modulePartNode);
        }

        /**
         * Generates the imports in the client file.
         */
        private NodeList<ImportDeclarationNode> generateImports() {
            List<ImportDeclarationNode> imports = new ArrayList<>();
            ImportDeclarationNode importForHttp = CodeGeneratorUtils.getImportDeclarationNode(
                    CodeGeneratorConstants.BALLERINA, CodeGeneratorConstants.HTTP);
            ImportDeclarationNode importForGraphql = CodeGeneratorUtils.getImportDeclarationNode(
                    CodeGeneratorConstants.BALLERINA, CodeGeneratorConstants.GRAPHQL);
            imports.add(importForHttp);
            imports.add(importForGraphql);
            return createNodeList(imports);
        }

        /**
         * Generates the members in the client file. The members include auth config record types & client class nodes.
         */
        private NodeList<ModuleMemberDeclarationNode> generateMembers(
                List<String> queryDocuments, GraphQLSchema graphQLSchema, AuthConfig authConfig)
                throws IOException {
            List<ModuleMemberDeclarationNode> members =  new ArrayList<>();
            // Generate client class
            members.add(generateClientClass(queryDocuments, graphQLSchema, authConfig));
            return createNodeList(members);
        }

        /**
         * Generates the client class in the client file.
         */
        private ClassDefinitionNode generateClientClass(List<String> queryDocuments,
                                                        GraphQLSchema graphQLSchema, AuthConfig authConfig)
                throws IOException {
            MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());
            NodeList<Token> classTypeQualifiers = createNodeList(
                    createToken(ISOLATED_KEYWORD), createToken(CLIENT_KEYWORD));
            IdentifierToken className = createIdentifierToken(getPluginClientClassName());

            List<Node> members =  new ArrayList<>();
            members.addAll(generateClassInstanceVariables(authConfig));
            members.add(generateInitFunction(authConfig));
            members.addAll(generateRemoteFunctions(queryDocuments, graphQLSchema, authConfig));

            return createClassDefinitionNode(metadataNode, createToken(PUBLIC_KEYWORD), classTypeQualifiers,
                    createToken(CLASS_KEYWORD), className, createToken(OPEN_BRACE_TOKEN),
                    createNodeList(members), createToken(CLOSE_BRACE_TOKEN), null);
        }

        /**
         * Gets the client class name for a given document.
         */
        public static String getPluginClientClassName() {
            return CodeGeneratorConstants.IDL_PLUGIN_CLIENT;
        }

        /**
         * Generates the client class instance variables.
         */
        private List<ObjectFieldNode> generateClassInstanceVariables(AuthConfig authConfig) {
            List<ObjectFieldNode> objectFields = new ArrayList<>();
            objectFields.add(generateGraphqlClientField());

            if (authConfig.isApiKeysConfig()) {
                objectFields.add(generateApiKeysConfigField());
            }
            return objectFields;
        }

        /**
         * Generates the client class init function.
         */
        private FunctionDefinitionNode generateInitFunction(AuthConfig authConfig) {
            MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());

            NodeList<Token> qualifierList = createNodeList(createToken(PUBLIC_KEYWORD), createToken(ISOLATED_KEYWORD));

            IdentifierToken functionName = createIdentifierToken(INIT);

            FunctionSignatureNode functionSignatureNode =
                    FunctionSignatureGenerator.getInstance().generateInitFunctionSignature(authConfig);
            FunctionBodyNode functionBodyNode =
                    FunctionBodyGenerator.getInstance().generateInitFunctionBody(authConfig);

            return createFunctionDefinitionNode(null, metadataNode, qualifierList, createToken(FUNCTION_KEYWORD),
                    functionName, createEmptyNodeList(), functionSignatureNode, functionBodyNode);
        }

        /**
         * Generates the client class remote functions.
         */
        private List<FunctionDefinitionNode> generateRemoteFunctions(List<String> queryDocuments,
                                                                     GraphQLSchema graphQLSchema, AuthConfig authConfig)
                throws IOException {
            List<FunctionDefinitionNode> functionDefinitionNodeList = new ArrayList<>();

            for (String document : queryDocuments) {
                Document queryDocument = io.ballerina.graphql.cmd.Utils.getGraphQLQueryDocument(document);
                QueryReader queryReader = new QueryReader(queryDocument);

                for (ExtendedOperationDefinition queryDefinition: queryReader.getExtendedOperationDefinitions()) {
                    // Generate remote function
                    FunctionDefinitionNode functionDefinitionNode =
                            generateRemoteFunction(queryDefinition, graphQLSchema, authConfig);
                    functionDefinitionNodeList.add(functionDefinitionNode);
                }
            }
            return functionDefinitionNodeList;
        }

        /**
         * Generates the GraphQL client {@code final graphql:Client graphqlClient;} instance variable.
         */
        private ObjectFieldNode generateGraphqlClientField() {
            MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());

            Token finalKeywordToken = createToken(FINAL_KEYWORD);
            NodeList<Token> qualifierList = createNodeList(finalKeywordToken);

            QualifiedNameReferenceNode typeName = createQualifiedNameReferenceNode(createIdentifierToken(GRAPHQL),
                    createToken(COLON_TOKEN), createIdentifierToken(CodeGeneratorConstants.CLIENT));

            IdentifierToken fieldName = createIdentifierToken(GRAPHQL_CLIENT);

            return createObjectFieldNode(metadataNode, null,
                    qualifierList, typeName, fieldName, null, null,
                    createToken(SEMICOLON_TOKEN));
        }

        /**
         * Generates the API keys config {@code final readonly & ApiKeysConfig apiKeysConfig;} instance variable.
         */
        private ObjectFieldNode generateApiKeysConfigField() {
            MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());

            NodeList<Token> qualifierList = createNodeList(createToken(FINAL_KEYWORD));

            TypeDescriptorNode readOnlyNode =
                    createTypeReferenceTypeDescNode(createSimpleNameReferenceNode(createToken(READONLY_KEYWORD)));
            TypeDescriptorNode apiKeysConfigNode =
                    createSimpleNameReferenceNode(createIdentifierToken(API_KEYS_CONFIG_TYPE_NAME));
            TypeDescriptorNode typeName = createIntersectionTypeDescriptorNode(readOnlyNode,
                    createToken(BITWISE_AND_TOKEN), apiKeysConfigNode);

            IdentifierToken fieldName = createIdentifierToken(API_KEYS_CONFIG_PARAM_NAME);

            return createObjectFieldNode(metadataNode, null,
                    qualifierList, typeName, fieldName, null, null,
                    createToken(SEMICOLON_TOKEN));
        }

        /**
         * Generates a client class remote function.
         */
        private FunctionDefinitionNode generateRemoteFunction(ExtendedOperationDefinition queryDefinition,
                                                              GraphQLSchema graphQLSchema, AuthConfig authConfig) {
            MetadataNode metadataNode = createMetadataNode(null, createEmptyNodeList());

            NodeList<Token> qualifierList = createNodeList(createToken(REMOTE_KEYWORD), createToken(ISOLATED_KEYWORD));

            IdentifierToken functionName = createIdentifierToken(queryDefinition.getName());

            FunctionSignatureNode functionSignatureNode =
                    FunctionSignatureGenerator.getInstance()
                            .generateRemoteFunctionSignature(queryDefinition, graphQLSchema);
            FunctionBodyNode functionBodyNode =
                    FunctionBodyGenerator.getInstance()
                            .generateRemoteFunctionBody(queryDefinition, graphQLSchema, authConfig);

            return createFunctionDefinitionNode(null, metadataNode, qualifierList, createToken(FUNCTION_KEYWORD),
                    functionName, createEmptyNodeList(), functionSignatureNode, functionBodyNode);
        }
    }
}
