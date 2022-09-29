package io.ballerina.graphql.idl.client;

import io.ballerina.graphql.cmd.GraphqlProject;
import io.ballerina.graphql.cmd.pojo.Config;
import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.ParseException;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.CodeGenerator;
import io.ballerina.graphql.generator.model.SrcFilePojo;
import io.ballerina.graphql.exception.GenerationException;
import io.ballerina.graphql.validator.QueryValidator;
import io.ballerina.graphql.validator.SDLValidator;
import io.ballerina.projects.*;
import io.ballerina.projects.plugins.IDLClientGenerator;
import io.ballerina.projects.plugins.IDLGeneratorPlugin;
import io.ballerina.projects.plugins.IDLPluginContext;
import io.ballerina.projects.plugins.IDLSourceGeneratorContext;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.ballerina.graphql.generator.CodeGeneratorConstants.CLIENT_FILE_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.CONFIG_TYPES_FILE_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.TYPES_FILE_NAME;
import static io.ballerina.graphql.generator.CodeGeneratorConstants.UTILS_FILE_NAME;

public class GraphQLToolIDLPlugin extends IDLGeneratorPlugin {
    
    @Override
    public void init(IDLPluginContext idlPluginContext) {
        idlPluginContext.addCodeGenerator(new GraphQLClientGenerator());
    }

    private static class GraphQLClientGenerator extends IDLClientGenerator {

        @Override
        public boolean canHandle(IDLSourceGeneratorContext idlSourceGeneratorContext ) {
            // check the validity of the yml file. For valid graphql file. Use configValidator API
            try {
                Config config = Utils.readConfig(idlSourceGeneratorContext.resourcePath().toString());
                List<GraphqlProject> projects = Utils.populateProjects(config, "./");
                if (projects.size() > 1) {
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
            String targetOutputPath = "./";
            String moduleName = "graphqlClient";
            try {
                Config config = Utils.readConfig(idlSourceGeneratorContext.resourcePath().toString());
                List<GraphqlProject> projects = Utils.populateProjects(config, targetOutputPath);
                List<SrcFilePojo> genSrcFiles = CodeGenerator.getInstance().generateBalSources(projects.get(0));
                ModuleId moduleId = ModuleId.create(moduleName, idlSourceGeneratorContext.currentPackage().packageId());
                List<DocumentConfig> documents = new ArrayList<>();

                genSrcFiles.stream().forEach(genSrcFile -> {
                    SrcFilePojo.GenFileType fileType = genSrcFile.getType();
                    switch (fileType) {
                        case GEN_SRC:
                            DocumentId documentId = DocumentId.create(CLIENT_FILE_NAME, moduleId);
                            DocumentConfig documentConfig = DocumentConfig.from(
                                    documentId, genSrcFile.getContent(), CLIENT_FILE_NAME);
                            documents.add(documentConfig);
                            break;
                        case MODEL_SRC:
                            DocumentId typeId = DocumentId.create(TYPES_FILE_NAME, moduleId);
                            DocumentConfig typeConfig = DocumentConfig.from(
                                    typeId, genSrcFile.getContent(), TYPES_FILE_NAME);
                            documents.add(typeConfig);
                            break;
                        case UTIL_SRC:
                            DocumentId utilId = DocumentId.create(UTILS_FILE_NAME, moduleId);
                            DocumentConfig utilConfig = DocumentConfig.from(
                                    utilId, genSrcFile.getContent(), UTILS_FILE_NAME);
                            documents.add(utilConfig);
                            break;
                        case CONFIG_SRC:
                            DocumentId configTypesId = DocumentId.create(CONFIG_TYPES_FILE_NAME, moduleId);
                            DocumentConfig configTypesConfigs = DocumentConfig.from(
                                    configTypesId, genSrcFile.getContent(), CONFIG_TYPES_FILE_NAME);
                            documents.add(configTypesConfigs);
                        default:
                            break;
                    }
                });
                ModuleDescriptor moduleDescriptor = ModuleDescriptor.from(
                        ModuleName.from(idlSourceGeneratorContext.currentPackage().packageName(), moduleName),
                        idlSourceGeneratorContext.currentPackage().descriptor());
                ModuleConfig moduleConfig = ModuleConfig.from(moduleId, moduleDescriptor, documents, Collections.emptyList(),
                        null, new ArrayList<>());
                idlSourceGeneratorContext.addClient(moduleConfig);
            } catch (GenerationException | IOException | ParseException | CmdException e) {
                e.printStackTrace();
            }
        }
    }
}
