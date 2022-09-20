package io.ballerina.graphql.idl.client;

import io.ballerina.graphql.cmd.GraphqlProject;
import io.ballerina.graphql.cmd.pojo.Config;
import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.ParseException;
import io.ballerina.graphql.generator.CodeGenerator;
import io.ballerina.graphql.generator.CodeGeneratorUtils;
import io.ballerina.graphql.generator.model.SrcFilePojo;
import io.ballerina.graphql.exception.GenerationException;
import io.ballerina.projects.*;
import io.ballerina.projects.plugins.IDLClientGenerator;
import io.ballerina.projects.plugins.IDLGeneratorPlugin;
import io.ballerina.projects.plugins.IDLPluginContext;
import io.ballerina.projects.plugins.IDLSourceGeneratorContext;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GraphQLToolIDLPlugin extends IDLGeneratorPlugin {
    
    @Override
    public void init(IDLPluginContext idlPluginContext) {
        idlPluginContext.addCodeGenerator(new GraphQLClientGenerator());
    }

    private static class GraphQLClientGenerator extends IDLClientGenerator {

        @Override
        public boolean canHandle(IDLSourceGeneratorContext idlSourceGeneratorContext ) {
            // check the validity of the yml file. For valid graphql file. Use configValidator API
            return true;
        }

        @Override
        public void perform(IDLSourceGeneratorContext idlSourceGeneratorContext) {
            // call client generation logic
            String gqlDocPath = "gqlConfig.yml";
            String targetOutputPath = "./";
            try {
                Config config = Utils.readConfig(gqlDocPath);
                List<GraphqlProject> projects = Utils.populateProjects(config, targetOutputPath);
                List<SrcFilePojo> genSrcFiles = CodeGenerator.getInstance().generateBalSources(projects.get(0));
                ModuleId moduleId = ModuleId.create("gqlClient", idlSourceGeneratorContext.currentPackage().packageId());
                List<DocumentConfig> documents = new ArrayList<>();
                genSrcFiles.stream().forEach(genSrcFile -> {
                    SrcFilePojo.GenFileType fileType = genSrcFile.getType();
                    switch (fileType) {
                        case GEN_SRC:
                            DocumentId documentId = DocumentId.create("client.bal", moduleId);  // This file name comes with .bal
                            DocumentConfig documentConfig = DocumentConfig.from(
                                    documentId, genSrcFile.getContent(), CodeGeneratorUtils.getClientFileName(documentFile));
                            documents.add(documentConfig);
                            break;
                        case MODEL_SRC:
                            DocumentId typeId = DocumentId.create("types.bal", moduleId);
                            DocumentConfig typeConfig = DocumentConfig.from(
                                    typeId, genSrcFile.getContent(), "types");
                            documents.add(typeConfig);
                            break;
                        case UTIL_SRC:
                            DocumentId utilId = DocumentId.create("utils.bal", moduleId);
                            DocumentConfig utilConfig = DocumentConfig.from(
                                    utilId, genSrcFile.getContent(), "utils");
                            documents.add(utilConfig);
                            break;
                        default:
                            break;
                    }
                });
                ModuleDescriptor moduleDescriptor = ModuleDescriptor.from(
                        ModuleName.from(idlSourceGeneratorContext.currentPackage().packageName(), "gqlClient"),
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
