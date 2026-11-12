/*
 * Copyright (c) 2026 WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.graphql.cmd.generator;

import graphql.schema.idl.errors.SchemaProblem;
import io.ballerina.graphql.cmd.Constants;
import io.ballerina.graphql.cmd.Utils;
import io.ballerina.graphql.cmd.config.BalGraphqlConfig;
import io.ballerina.graphql.cmd.config.SchemaConfig;
import io.ballerina.graphql.cmd.config.SchemaSource;
import io.ballerina.graphql.exception.GenerationException;
import io.ballerina.graphql.exception.SDLValidationException;
import io.ballerina.graphql.exception.ValidationException;
import io.ballerina.graphql.generator.client.exception.IntospectionException;
import io.ballerina.graphql.generator.service.GraphqlServiceProject;
import io.ballerina.graphql.generator.service.ServiceBaseName;
import io.ballerina.graphql.generator.service.diagnostic.ServiceDiagnosticMessages;
import io.ballerina.graphql.generator.service.exception.ServiceGenerationException;
import io.ballerina.graphql.generator.service.generator.ServiceCodeGenerator;
import io.ballerina.graphql.generator.service.merge.MergeResult;
import io.ballerina.graphql.generator.service.merge.MergeResult.MergedResolver;
import io.ballerina.graphql.generator.service.merge.MergeResult.Outcome;
import io.ballerina.graphql.generator.service.merge.ServiceMerger;
import io.ballerina.graphql.generator.service.merge.TypesMerger;
import io.ballerina.graphql.generator.utils.CodeGeneratorUtils;
import io.ballerina.graphql.generator.utils.SrcFilePojo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.ballerina.graphql.generator.CodeGeneratorConstants.ROOT_PROJECT_NAME;

/**
 * Generates a Ballerina service for a given GraphQL schema file.
 */
public class ServiceGeneration implements Generator {

    private static final String SUCCESS_MESSAGE =
            "The service generation process is complete. The following files were written.";
    private static final String CREATED_FILE_FORMAT = "-- %s";
    private static final Set<String> GENERATED_FILES =
            Set.of(ServiceCodeGenerator.TYPES_FILE_NAME, ServiceCodeGenerator.SERVICE_FILE_NAME);
    private static final String RESOLVER_SUMMARY = "Resolvers: %d added, %d kept.";
    private static final String ADDED_RESOLVER_LINE = "  added     %s";
    private static final String WARNING_OUTDATED_RESOLVERS =
            "Warning: Resolvers whose signature does not match the schema: %s. Update them.";
    private static final String QUOTE = "\"";
    private static final String WARNING_TYPES_NOT_IN_SCHEMA =
            "Warning: Types not in the schema: %s. Delete them if they are no longer needed.";
    private static final String LIST_SEPARATOR = ", ";
    private static final String ERROR_CANNOT_READ = "Cannot read \"%s\" to merge it. Check its permissions.";

    private final GenerationContext context;
    private final BalGraphqlConfig balGraphqlConfig;
    private final ServiceCodeGenerator serviceCodeGenerator;
    private final List<MergedResolver> mergedResolvers = new ArrayList<>();
    private final List<String> typesNotInSchema = new ArrayList<>();
    private PackageOutput packageOutput;
    private Path outputPath;
    private GraphqlServiceProject project;
    private List<SrcFilePojo> sources;
    private String licenseHeader;

    public ServiceGeneration(GenerationContext context, BalGraphqlConfig balGraphqlConfig) {
        this.context = context;
        this.balGraphqlConfig = balGraphqlConfig;
        this.serviceCodeGenerator = new ServiceCodeGenerator(context.isUseRecordsForObjects(), idTypes(),
                dataLoaders());
    }

    private Map<String, String> idTypes() {
        if (this.balGraphqlConfig == null || this.balGraphqlConfig.idTypes() == null) {
            return Map.of();
        }
        return this.balGraphqlConfig.idTypes();
    }

    private Map<String, String> dataLoaders() {
        if (this.balGraphqlConfig == null || this.balGraphqlConfig.dataloaders() == null) {
            return Map.of();
        }
        return this.balGraphqlConfig.dataloaders();
    }

    @Override
    public void validate() throws GenerationException {
        this.packageOutput = PackageOutput.resolve(context.getTargetOutputPath(), context.getModuleName());
        this.outputPath = this.packageOutput.outputPath();
        this.licenseHeader = LicenseHeader.read(context.getLicensePath(), context.getExecutionPath());
        if (this.balGraphqlConfig != null
                && this.balGraphqlConfig.schema().source() != SchemaSource.FILE) {
            validateFromRemoteSchema(this.balGraphqlConfig.schema());
            return;
        }
        validateFromFile();
    }

    private void validateFromFile() throws GenerationException {
        String schemaPath = resolveSchemaPath();
        File graphqlFile = new File(schemaPath);
        if (!graphqlFile.exists()) {
            throw new GenerationException(new ServiceGenerationException(
                    ServiceDiagnosticMessages.GRAPHQL_SERVICE_GEN_100, null,
                    String.format(Constants.MESSAGE_MISSING_SCHEMA_FILE, schemaPath)));
        }
        if (!graphqlFile.canRead()) {
            throw new GenerationException(new ServiceGenerationException(
                    ServiceDiagnosticMessages.GRAPHQL_SERVICE_GEN_100, null,
                    String.format(Constants.MESSAGE_CAN_NOT_READ_SCHEMA_FILE, schemaPath)));
        }
        this.project = new GraphqlServiceProject(ROOT_PROJECT_NAME, schemaPath, this.outputPath.toString());
        try {
            Utils.validateGraphqlProject(this.project);
        } catch (IOException | ValidationException e) {
            throw new GenerationException(e);
        }
    }

    private void validateFromRemoteSchema(SchemaConfig schemaConfig) throws GenerationException {
        String schemaLocation = schemaConfig.source() == SchemaSource.URL
                ? schemaConfig.url() : schemaConfig.endpoint();
        this.project = new GraphqlServiceProject(ROOT_PROJECT_NAME, schemaLocation, this.outputPath.toString());
        try {
            this.project.setGraphQLSchema(Utils.resolveGraphQLSchema(schemaConfig));
        } catch (IntospectionException e) {
            throw new GenerationException(new ValidationException(e.getMessage(), this.project.getName()));
        } catch (SchemaProblem e) {
            throw new GenerationException(new SDLValidationException("GraphQL SDL validation failed.",
                    e.getErrors(), this.project.getName()));
        }
    }

    private String resolveSchemaPath() {
        if (this.balGraphqlConfig == null) {
            return context.getInputPath();
        }
        Path configDirectory = Paths.get(context.getInputPath()).toAbsolutePath().getParent();
        return configDirectory.resolve(this.balGraphqlConfig.schema().path()).normalize().toString();
    }

    @Override
    public void generate() throws GenerationException {
        try {
            this.sources = this.serviceCodeGenerator.generateBalSources(this.project, serviceBaseName(),
                    ModuleDefinitions.read(this.outputPath, GENERATED_FILES));
            if (context.isMerge()) {
                this.sources = mergeIntoExistingFiles(this.sources);
            }
            // --license and --merge are not used together, so the header is only added to files written whole.
            this.sources = LicenseHeader.addTo(this.sources, this.licenseHeader);
        } catch (ServiceGenerationException e) {
            throw new GenerationException(e);
        }
    }

    // The merge runs before the output files are checked and the user is asked, so a file that cannot be merged
    // stops the run before anything is asked or written (FF-4). A file that does not exist yet is created as
    // generated, and a directory in its place is left to the output file checks.
    private List<SrcFilePojo> mergeIntoExistingFiles(List<SrcFilePojo> generatedSources)
            throws ServiceGenerationException {
        List<SrcFilePojo> mergedSources = new ArrayList<>();
        for (SrcFilePojo source : generatedSources) {
            Path filePath = CodeGeneratorUtils.resolveFilePath(source, this.outputPath);
            if (!Files.isRegularFile(filePath)) {
                mergedSources.add(source);
                continue;
            }
            MergeResult result = merge(source, readExistingFile(filePath));
            this.mergedResolvers.addAll(result.resolvers());
            this.typesNotInSchema.addAll(result.typesNotInSchema());
            mergedSources.add(new SrcFilePojo(source.getType(), source.getModuleName(), source.getFileName(),
                    result.source()));
        }
        return mergedSources;
    }

    private static MergeResult merge(SrcFilePojo source, String existingSource) throws ServiceGenerationException {
        if (ServiceCodeGenerator.SERVICE_FILE_NAME.equals(source.getFileName())) {
            return ServiceMerger.merge(existingSource, source.getContent());
        }
        return TypesMerger.merge(existingSource, source.getContent());
    }

    private static String readExistingFile(Path filePath) throws ServiceGenerationException {
        try {
            return Files.readString(filePath);
        } catch (IOException e) {
            throw new ServiceGenerationException(ServiceDiagnosticMessages.GRAPHQL_SERVICE_GEN_100, null,
                    String.format(ERROR_CANNOT_READ, OutputFileGuard.displayPath(filePath)));
        }
    }

    // A count of the added and kept resolvers and the added ones, then a warning for the kept resolvers whose
    // signature no longer matches the schema and for the types the schema does not have.
    private List<String> mergeSummary() {
        List<String> lines = new ArrayList<>();
        if (!this.mergedResolvers.isEmpty()) {
            lines.add(String.format(RESOLVER_SUMMARY, count(Outcome.ADDED),
                    count(Outcome.KEPT) + count(Outcome.OUTDATED)));
            resolverKeys(Outcome.ADDED).forEach(key -> lines.add(String.format(ADDED_RESOLVER_LINE, key)));
        }
        List<String> outdated = resolverKeys(Outcome.OUTDATED);
        if (!outdated.isEmpty()) {
            lines.add(String.format(WARNING_OUTDATED_RESOLVERS, outdated.stream()
                    .map(key -> QUOTE + key + QUOTE).collect(Collectors.joining(LIST_SEPARATOR))));
        }
        if (!this.typesNotInSchema.isEmpty()) {
            lines.add(String.format(WARNING_TYPES_NOT_IN_SCHEMA, String.join(LIST_SEPARATOR, this.typesNotInSchema)));
        }
        return lines;
    }

    private long count(Outcome outcome) {
        return resolverKeys(outcome).size();
    }

    private List<String> resolverKeys(Outcome outcome) {
        return this.mergedResolvers.stream()
                .filter(resolver -> resolver.outcome() == outcome)
                .map(MergedResolver::key)
                .toList();
    }

    private ServiceBaseName serviceBaseName() {
        if (context.getServiceName() != null) {
            return new ServiceBaseName(context.getServiceName(), ServiceBaseName.Source.GIVEN);
        }
        if (this.balGraphqlConfig == null || this.balGraphqlConfig.schema().source() == SchemaSource.FILE) {
            return new ServiceBaseName(this.project.getFileName(), ServiceBaseName.Source.SCHEMA_FILE);
        }
        if (context.getModuleName() != null) {
            return new ServiceBaseName(context.getModuleName(), ServiceBaseName.Source.MODULE);
        }
        return this.packageOutput.packageName()
                .map(packageName -> new ServiceBaseName(packageName, ServiceBaseName.Source.PACKAGE))
                .orElseGet(() -> new ServiceBaseName(this.packageOutput.directoryName(),
                        ServiceBaseName.Source.DIRECTORY));
    }

    @Override
    public List<Path> outputFiles() {
        // Only the sources writeGeneratedSources writes are listed.
        return this.sources.stream()
                .filter(source -> source.getType().isOverwritable())
                .map(source -> CodeGeneratorUtils.resolveFilePath(source, outputPath))
                .toList();
    }

    @Override
    public void write() throws GenerationException {
        try {
            this.serviceCodeGenerator.writeGeneratedSources(this.sources, outputPath);
        } catch (IOException e) {
            throw new GenerationException(new ServiceGenerationException(
                    ServiceDiagnosticMessages.GRAPHQL_SERVICE_GEN_100, null, e.getMessage()));
        }
        context.getOutStream().println(SUCCESS_MESSAGE);
        outputFiles().forEach(filePath -> context.getOutStream().println(String.format(CREATED_FILE_FORMAT,
                OutputFileGuard.displayPath(filePath))));
    }

    // What the merge keeps and adds, shown before the user is asked (FF-5).
    @Override
    public List<String> plan() {
        return mergeSummary();
    }

    @Override
    public boolean canMerge() {
        return true;
    }
}
