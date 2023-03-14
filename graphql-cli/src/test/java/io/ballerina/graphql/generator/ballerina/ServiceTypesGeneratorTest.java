package io.ballerina.graphql.generator.ballerina;

import graphql.schema.GraphQLSchema;
import io.ballerina.graphql.cmd.GraphqlServiceProject;
import io.ballerina.graphql.common.GraphqlTest;
import io.ballerina.graphql.common.TestUtils;
import io.ballerina.graphql.exception.CmdException;
import io.ballerina.graphql.exception.ParseException;
import io.ballerina.graphql.exception.ServiceTypesGenerationException;
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
            throws CmdException, IOException, ParseException, ValidationException, ServiceTypesGenerationException {
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
//importballerina/graphql;typeSchema01Apiserviceobject{*graphql:Service;resourcefunctiongetbook(intid,string?title)returnsBook?;resourcefunctiongetbooks()returnsBook[]?;};serviceclassBook{resourcefunctiongetid()returnsint{}resourcefunctiongettitle()returnsstring{}}
    @Test(description = "Test for schema with more types, method - default")
    public void testGenerateSrcForMoreTypes()
            throws CmdException, IOException, ParseException, ValidationException, ServiceTypesGenerationException {
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
//importballerina/graphql;typeSchema02Apiserviceobject{*graphql:Service;resourcefunctiongetbook(intid,string?title)returnsBook?;resourcefunctiongetbooks()returnsBook?[]?;resourcefunctiongetauthors()returnsAuthor[];};serviceclassAuthor{resourcefunctiongetid()returnsint{}resourcefunctiongetname()returnsstring{}}serviceclassBook{resourcefunctiongetid()returnsint{}resourcefunctiongettitle()returnsstring{}}
//importballerina/graphql;typeSchema02Apiserviceobject{*graphql:Service;resourcefunctiongetbook(intid,string?title)returnsBook?;resourcefunctiongetbooks()returnsBook?[]?;resourcefunctiongetauthors()returnsAuthor[];};serviceclassAuthor{resourcefunctiongetid()returnsint{}resourcefunctiongetname()returnsstring{}}serviceclassBook{resourcefunctiongetid()returnsint{}resourcefunctiongettitle()returnsstring{}}
    @Test(description = "Test for schema with mutation types, method - default")
    public void testGenerateSrcForSchemaWithMutationTypes()
            throws CmdException, IOException, ParseException, ValidationException, ServiceTypesGenerationException {
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
//importballerina/graphql;typeSchema04Apiserviceobject{*graphql:Service;resourcefunctiongetbook(intid,string?title)returnsBook?;resourcefunctiongetbooks()returnsBook?[]?;resourcefunctiongetauthors()returnsAuthor[];remotefunctionaddBook(stringtitle,intauthorId)returnsBook?;};serviceclassAuthor{resourcefunctiongetid()returnsint{}resourcefunctiongetname()returnsstring{}}serviceclassBook{resourcefunctiongetid()returnsint{}resourcefunctiongettitle()returnsstring{}}
//importballerina/graphql;typeSchema04Apiserviceobject{*graphql:Service;resourcefunctiongetbook(intid,string?title)returnsBook?;resourcefunctiongetbooks()returnsBook?[]?;resourcefunctiongetauthors()returnsAuthor[];remotefunctionaddBook(stringtitle,intauthorId)returnsBook?;};serviceclassAuthor{resourcefunctiongetid()returnsint{}resourcefunctiongetname()returnsstring{}}serviceclassBook{resourcefunctiongetid()returnsint{}resourcefunctiongettitle()returnsstring{}}

    @Test(description = "Test for schema with subscription types, method - default")
    public void testGenerateSrcForSchemaWithSubscriptionTypes()
            throws CmdException, IOException, ParseException, ValidationException, ServiceTypesGenerationException {
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
            throws CmdException, IOException, ParseException, ValidationException, ServiceTypesGenerationException {
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
            throws CmdException, IOException, ParseException, ValidationException, ServiceTypesGenerationException {
        String fileName = "Schema06Api";
        String expectedFile = "types06RecordObjects.bal";

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
//importballerina/graphql;typeSchema06Apiserviceobject{*graphql:Service;resourcefunctiongetdog(stringname)returnsDog?;resourcefunctiongetcat(stringname)returnsCat?;};serviceclassCat{resourcefunctiongetname()returnsstring{}resourcefunctiongetage()returnsint{}}typeDogrecord{stringname;booleanknowsWord;};
//importballerina/graphql;typeSchema06Apiserviceobject{*graphql:Service;resourcefunctiongetdog(stringname)returnsDog?;resourcefunctiongetcat(stringname)returnsCat?;};typeCatrecord{stringname;intage;};serviceclassDog{resourcefunctiongetname()returnsstring{}resourcefunctiongetknowsWord(stringword)returnsboolean{}}

    @Test(description = "Test for simple schema with enum, method - default")
    public void testGenerateSrcForEnum()
            throws CmdException, IOException, ParseException, ValidationException, ServiceTypesGenerationException {
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
//importballerina/graphql;typeSchema07Apiserviceobject{*graphql:Service;resourcefunctiongetstudent(intid)returnsStudent?;};enumGender{MALE,FEMALE}serviceclassStudent{resourcefunctiongetid()returnsint{}resourcefunctiongetname()returnsstring{}}
//importballerina/graphql;typeSchema07Apiserviceobject{*graphql:Service;resourcefunctiongetstudent(intid)returnsStudent?;};serviceclassStudent{resourcefunctiongetid()returnsint{}resourcefunctiongetname()returnsstring{}}enumGender{MALE,FEMALE}
    @Test(description = "Test for simple schema with union, method - default")
    public void testGenerateSrcForUnion() throws CmdException, IOException, ParseException, ValidationException,
            ServiceTypesGenerationException {
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
            ServiceTypesGenerationException {
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

    @Test(description = "Test for simple schema with multiple interfaces, method - default")
    public void testGenerateSrcForMultipleInterface() throws CmdException, IOException, ParseException,
            ValidationException,
            ServiceTypesGenerationException {
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

    @Test(description = "Test for schema with interfaces implementing interfaces, method - default")
    public void testGenerateSrcForInterfacesImplementingInterfaces() throws CmdException, IOException, ParseException,
            ValidationException,
            ServiceTypesGenerationException {
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

    @Test(description = "Test for schema multi-dimensional lists, method - default")
    public void testGenerateSrcForSchemaWithMultiDimensionalLists() throws CmdException, IOException, ParseException,
            ValidationException,
            ServiceTypesGenerationException {
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

//importballerina/graphql;typeSchema09Apiserviceobject{*graphql:Service;resourcefunctiongetstudent(intid)returnsStudent?;};typeInfodistinctserviceobject{resourcefunctiongetname()returnsstring;};serviceclassBook{resourcefunctiongetname()returnsstring{}}distinctserviceclassTeacher{*Info;resourcefunctiongetid()returnsint{}resourcefunctiongetname()returnsstring{}}distinctserviceclassStudent{*Info;resourcefunctiongetid()returnsint{}resourcefunctiongetname()returnsstring{}}
//importballerina/graphql;typeSchema09Apiserviceobject{*graphql:Service;resourcefunctiongetstudent(intid)returnsStudent?;};typeInfodistinctserviceobject{resourcefunctiongetname()returnsstring;};serviceclassBook{resourcefunctiongetname()returnsstring{}}distinctserviceclassStudent{*Info;resourcefunctiongetid()returnsint{}resourcefunctiongetname()returnsstring{}}distinctserviceclassTeacher{*Info;resourcefunctiongetid()returnsint{}resourcefunctiongetname()returnsstring{}}