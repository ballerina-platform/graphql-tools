package io.ballerina.graphql.generator.ballerina;

import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.cmd.GraphqlServiceProject;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.common.TestUtils;
import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.ParseException;
import io.ballerina.graphql.exception.TypesGenerationException;
import io.ballerina.graphql.exception.ValidationException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test class for ServiceTypesGenerator.
 * Test the successful generation of service types
 */
public class ServiceTypesGeneratorTest extends GraphqlTest {
    @Test(description = "Test for simple schema, method - default")
    public void testGenerateSrc()
            throws CmdException, IOException, ParseException, ValidationException, TypesGenerationException {
        String fileName = "Schema01Api";
        String expectedFile = "types01Default.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid",
                        fileName + ".graphql")).toString(),
                this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(fileName, graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContent);
    }

    @Test(description = "Test for schema with more types, method - default")
    public void testGenerateSrcForMoreTypes()
            throws CmdException, IOException, ParseException, ValidationException, TypesGenerationException {
        String fileName = "Schema02Api";
        String expectedFile = "types02Default.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid",
                        fileName + ".graphql")).toString(),
                this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(fileName, graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContent);
    }

    @Test(description = "Test for schema with mutation types, method - default")
    public void testGenerateSrcForSchemaWithMutationTypes()
            throws CmdException, IOException, ParseException, ValidationException, TypesGenerationException {
        String fileName = "Schema04Api";
        String expectedFolder = "service04";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid",
                        fileName + ".graphql")).toString(),
                this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(fileName, graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices",
                expectedFolder, "types.bal"));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContent);
    }

//importballerina/graphql;typeSchema04Apiserviceobject{*graphql:Service;resourcefunctiongetbook(intid,string?title)returnsBook?;resourcefunctiongetbooks()returnsBook?[]?;resourcefunctiongetauthors()returnsAuthor[];remotefunctionaddBook(stringtitle,intauthorId)returnsstream<Book?>;};serviceclassAuthor{resourcefunctiongetid()returnsint{}resourcefunctiongetname()returnsstring{}}serviceclassBook{resourcefunctiongetid()returnsint{}resourcefunctiongettitle()returnsstring{}}
//importballerina/graphql;typeSchema04Apiserviceobject{*graphql:Service;resourcefunctiongetbook(intid,string?title)returnsBook?;resourcefunctiongetbooks()returnsBook?[]?;resourcefunctiongetauthors()returnsAuthor[];remotefunctionaddBook(stringtitle,intauthorId)returnsBook?;};serviceclassAuthor{resourcefunctiongetid()returnsint{}resourcefunctiongetname()returnsstring{}}serviceclassBook{resourcefunctiongetid()returnsint{}resourcefunctiongettitle()returnsstring{}}

    @Test(description = "Test for schema with subscription types, method - default")
    public void testGenerateSrcForSchemaWithSubscriptionTypes()
            throws CmdException, IOException, ParseException, ValidationException, TypesGenerationException {
        String fileName = "Schema05Api";
        String expectedFolder = "service05";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid",
                        fileName + ".graphql")).toString(),
                this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(fileName, graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices",
                expectedFolder, "types.bal"));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContent);
    }

    @Test(description = "Test for schema with input types, method - default")
    public void testGenerateSrcForSchemaWithInputTypes()
            throws CmdException, IOException, ParseException, ValidationException, TypesGenerationException {
        String fileName = "Schema03Api";
        String expectedFile = "types03Default.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid",
                        fileName + ".graphql")).toString(),
                this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(fileName, graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContent);
    }

    @Test(description = "Test for simple schema, method - record forced")
    public void testGenerateSrcForRecordForced()
            throws CmdException, IOException, ParseException, ValidationException, TypesGenerationException {
        String fileName = "Schema06Api";
        String expectedFile = "types06RecordsForced.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid",
                        fileName + ".graphql")).toString(),
                this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        serviceTypesGenerator.setRecordForced(true);
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(fileName, graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContent);
    }

    @Test(description = "Test for simple schema with enum, method - default")
    public void testGenerateSrcForEnum()
            throws CmdException, IOException, ParseException, ValidationException, TypesGenerationException {
        String fileName = "Schema07Api";
        String expectedFile = "types07Default.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid",
                        fileName + ".graphql")).toString(),
                this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(fileName, graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContent);
    }

    @Test(description = "Test for simple schema with union, method - default")
    public void testGenerateSrcForUnion() throws CmdException, IOException, ParseException, ValidationException,
            TypesGenerationException {
        String fileName = "Schema08Api";
        String expectedFile = "types08Default.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid",
                        fileName + ".graphql")).toString(),
                this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(fileName, graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContent);
    }

    @Test(description = "Test for simple schema with interface, method - default")
    public void testGenerateSrcForInterface() throws CmdException, IOException, ParseException, ValidationException,
            TypesGenerationException {
        String fileName = "Schema09Api";
        String expectedFile = "types09Default.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid",
                        fileName + ".graphql")).toString(),
                this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(fileName, graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContent);
    }

    @Test(description = "Test for simple schema with multiple interfaces implemented, method - default")
    public void testGenerateSrcForMultipleInterfaces() throws CmdException, IOException, ParseException, ValidationException,
            TypesGenerationException {
        String fileName = "Schema10Api";
        String expectedFile = "types10Default.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid",
                        fileName + ".graphql")).toString(),
                this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(fileName, graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContent);
    }

    @Test(description = "Test for simple schema with interface implementing interface, method - default")
    public void testGenerateSrcForInterfaceImplementingInterface() throws CmdException, IOException, ParseException,
            ValidationException, TypesGenerationException {
        String fileName = "Schema11Api";
        String expectedFile = "types11Default.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid",
                        fileName + ".graphql")).toString(),
                this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(fileName, graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContent);
    }

    @Test(description = "Test for simple schema with interface implementing interface, method - default")
    public void testGenerateSrcForSchemaWithMultiDimensionList() throws CmdException, IOException, ParseException,
            ValidationException, TypesGenerationException {
        String fileName = "Schema12Api";
        String expectedFile = "types12Default.bal";

        GraphqlServiceProject project = TestUtils.getValidatedMockServiceProject(
                this.resourceDir.resolve(Paths.get("serviceGen", "graphqlSchemas", "valid",
                        fileName + ".graphql")).toString(),
                this.tmpDir);
        GraphQLSchema graphQLSchema = project.getGraphQLSchema();

        ServiceTypesGenerator serviceTypesGenerator = new ServiceTypesGenerator();
        String generatedServiceTypesContent =
                serviceTypesGenerator.generateSrc(fileName, graphQLSchema).trim().replaceAll("\\s+", "")
                        .replaceAll(System.lineSeparator(), "");

        Path expectedServiceTypesFile = resourceDir.resolve(Paths.get("serviceGen", "expectedServices", expectedFile));
        String expectedServiceTypesContent = readContent(expectedServiceTypesFile);

        Assert.assertEquals(expectedServiceTypesContent, generatedServiceTypesContent);
    }
}

//importballerina/graphql;typeSchema11Apiserviceobject{*graphql:Service;resourcefunctiongetstudent(intid)returnsStudent?;};typeAddressInfodistinctserviceobject{*ContactInfo;resourcefunctiongetaddress()returnsstring;};typeContactInfodistinctserviceobject{*Info;resourcefunctiongetemail()returnsstring;};typeInfodistinctserviceobject{resourcefunctiongetname()returnsstring;};distinctserviceclassStudent{*Info;resourcefunctiongetid()returnsint{}resourcefunctiongetname()returnsstring{}}distinctserviceclassTeacher{*AddressInfo;*ContactInfo;*Info;resourcefunctiongetid()returnsint{}resourcefunctiongetname()returnsstring{}resourcefunctiongetemail()returnsstring{}resourcefunctiongetaddress()returnsstring{}}
//importballerina/graphql;typeSchema11Apiserviceobject{*graphql:Service;resourcefunctiongetstudent(intid)returnsStudent?;};typeAddressInfodistinctserviceobject{*ContactInfo;resourcefunctiongetaddress()returnsstring;};typeContactInfodistinctserviceobject{*Info;resourcefunctiongetemail()returnsstring;};typeInfodistinctserviceobject{resourcefunctiongetname()returnsstring;};distinctserviceclassStudent{*Info;resourcefunctiongetid()returnsint{}resourcefunctiongetname()returnsstring{}}distinctserviceclassTeacher{*AddressInfo;resourcefunctiongetid()returnsint{}resourcefunctiongetname()returnsstring{}resourcefunctiongetemail()returnsstring{}resourcefunctiongetaddress()returnsstring{}}