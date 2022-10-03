package io.ballerina.graphql.idl.client;

import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.graphql.cmd.GraphqlProject;
import io.ballerina.graphql.cmd.pojo.Config;
import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.GenerationException;
import io.ballerina.graphql.exception.ParseException;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.CodeGenerator;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
                List<GraphqlProject> projects = Utils.populateProjects(config, "./");
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
            // call client generation logic
//            String gqlDocPath = "gqlConfig.yml";
            String targetOutputPath = "./generated";
            String moduleName = "graphql_client";
            try {
                Config config = Utils.readConfig(idlSourceGeneratorContext.resourcePath().toString());
                List<GraphqlProject> projects = Utils.populateProjects(config, targetOutputPath);
                List<SrcFilePojo> genSrcFiles = CodeGenerator.getInstance().generateBalSources(projects.get(0));
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
            } catch (GenerationException | IOException | ParseException | CmdException e) {
                Constants.DiagnosticMessages error = Constants.DiagnosticMessages.ERROR_WHILE_GENERATING_CLIENT;
                Utils.reportDiagnostic(idlSourceGeneratorContext, error,
                        idlSourceGeneratorContext.clientNode().location());
            }
        }
    }
}
