package io.ballerina.graphql.idl.client;

import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.graphql.cmd.GraphqlProject;
import io.ballerina.graphql.cmd.pojo.Config;
import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.GenerationException;
import io.ballerina.graphql.exception.ParseException;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.CodeGenerator;
import io.ballerina.graphql.generator.GeneratedContext;
import io.ballerina.graphql.generator.model.SrcFilePojo;
import io.ballerina.graphql.idl.exception.DiagnosticMessages;
import io.ballerina.graphql.idl.exception.IDLMultipleProjectException;
import io.ballerina.graphql.validator.ConfigValidator;
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

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.ballerina.graphql.cmd.Constants.YAML_EXTENSION;
import static io.ballerina.graphql.cmd.Constants.YML_EXTENSION;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.EMPTY_STRING;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.IDL_MODULE_NAME;

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
            String filePath = idlSourceGeneratorContext.resourcePath().toString();
            if (filePath.endsWith(YML_EXTENSION) || filePath.endsWith(YAML_EXTENSION)) {
                try {
                    String content = Files.readString(idlSourceGeneratorContext.resourcePath());
                    Pattern pattern1 = Pattern.compile("schema");
                    Pattern pattern2 = Pattern.compile("documents");
                    Matcher matcher1 = pattern1.matcher(content);
                    Matcher matcher2 = pattern2.matcher(content);
                    return matcher1.find() && matcher2.find();
                } catch (IOException e) {
                    return false;
                }
            } else {
                return false;
            }
        }

        @Override
        public void perform(IDLSourceGeneratorContext idlSourceGeneratorContext) {
            String moduleName = IDL_MODULE_NAME;
            try {
                Config config = Utils.readConfig(idlSourceGeneratorContext.resourcePath().toString());
                ConfigValidator.getInstance().validate(config);
                List<GraphqlProject> projects = Utils.populateProjects(config, EMPTY_STRING);
                if (projects.size() > 1) {
                    throw new IDLMultipleProjectException(
                            DiagnosticMessages.GRAPHQL_IDL_CLIENT_100.getDescription());
                }
                SDLValidator.getInstance().validate(projects.get(0));
                QueryValidator.getInstance().validate(projects.get(0));
                List<SrcFilePojo> genSrcFiles = CodeGenerator.getInstance().generateBalSources(projects.get(0),
                        GeneratedContext.IDL_PLUGIN);
                ModuleId moduleId = ModuleId.create(moduleName, idlSourceGeneratorContext.currentPackage().packageId());
                LinkedList<DocumentConfig> documents = new LinkedList<>();

                genSrcFiles.forEach(genSrcFile -> {
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

            } catch (GenerationException | IOException | ParseException | CmdException e) {
                Constants.DiagnosticMessages error = Constants.DiagnosticMessages.ERROR_WHILE_GENERATING_CLIENT;
                Utils.reportDiagnostic(idlSourceGeneratorContext, error,
                        idlSourceGeneratorContext.clientNode().location());
            } catch (IDLMultipleProjectException e) {
                Constants.DiagnosticMessages error = Constants.DiagnosticMessages.ERROR_MULTIPLE_PROJECT_AVAILABILITY;
                Utils.reportDiagnostic(idlSourceGeneratorContext, error,
                        idlSourceGeneratorContext.clientNode().location());
            } catch (ValidationException e) {
                Constants.DiagnosticMessages error = Constants.DiagnosticMessages.ERROR_WHILE_VALIDATING;
                Utils.reportDiagnostic(idlSourceGeneratorContext, error,
                        idlSourceGeneratorContext.clientNode().location());
            }
        }
    }
}
