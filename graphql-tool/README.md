# Ballerina GraphQL CLI tool

Every GraphQL service defines a set of types that completely describe the set of possible data you can query on that service. Then, when queries come in, they are validated and executed against that schema. GraphQL schemas for a service are now most often specified using what’s known as the `GraphQL SDL` (schema definition language), also sometimes referred to as just GraphQL schema language. The GraphQL tooling supports generating the GraphQL schema specified by Schema Definition Language for a given Ballerina GraphQL service. The user will be able to generate the schema and export it to a specific directory for a selected set of services in the given bal file. Also, the GraphQL tooling makes it easy to start the development of a service in Ballerina for a given GraphQL schema by generating Ballerina service skeletons. In addition, Ballerina GraphQL tooling will make it easy for you to start the development of a client in Ballerina for a given GraphQL SDL and a GraphQL document configured in a GraphQL config file by generating Ballerina client skeletons. You can generate a single client in Ballerina for multiple GraphQL documents for a given GraphQL SDL. It also enables you to generate multiple Ballerina modules for multiple GraphQL projects to work with different GraphQL APIs. The GraphQL client generation is an `experimental` feature which supports only a limited set of functionality.

The Ballerina GraphQL tooling support provides the following capabilities.

1. Generate the GraphQL schema specified by Schema Definition Language for a given Ballerina GraphQL service(s).

2. Generate a Ballerina service from a given GraphQL schema specified by Schema Definition Language.

3. Generating a Ballerina client from a given GraphQL config file configured with a GraphQL schema specified by Schema Definition Language and a GraphQL document.

4. Generating a Ballerina client from a given GraphQL config file configured with a GraphQL schema specified by Schema Definition Language and multiple GraphQL documents.

5. Generating multiple Ballerina modules from a given GraphQL config file configured with multiple GraphQL projects. Each project will generate a separate Ballerina module. This enables you to work with multiple GraphQL APIs by configuring each GraphQL API under a separate project.

### Command for GraphQL schema generation

The `graphql` command for GraphQL schema generation and the usages are as follows.

```
bal graphql [-i | --input] <graphql-service-file-path> [-o | --output] <output-location> [-s | --service] <service-base-path>
```

| Argument      | Description   |
| ------------- | ------------- |
| -i, --input   | The `input` parameter specifies the path of the Ballerina GraphQL service file (e.g., service.bal). This parameter is mandatory. |
| -o, --output  | The `output` parameter specifies the path of the output location of the generated GraphQL schema files. This parameter is optional. If this parameter is not specified, the schema files will be generated at the same location from which the GraphQL command is executed. |
| -s, --service  | The `service` parameter specifies the base path of the Ballerina GraphQL service which the schema is needed to be generated. This parameter is optional. If this parameter is not specified, the schema files will be generated for all the Ballerina GraphQL services declared in the input file. |

### Command for Ballerina GraphQL service generation

The `graphql` command for Ballerina Graphql service generation and the usages are as follows.

```
bal graphql [-i | --input] <graphql-schema-file-path> [-o | --output] <output-location> [-m | --mode] <operation-mode> [-r | --use-records-for-objects]
```


| Argument                      | Description                                                                                                                                                                                                                                                                                                                                                                    |
|-------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| -i, --input                   | The `input` parameter specifies the path of the GraphQL schema file (e.g., schema.graphql). This parameter is mandatory.                                                                                                                                                                                                                                                       |
| -o, --output                  | The `output` parameter specifies the path of the output location of the generated Ballerina GraphQL service files. This parameter is optional. If this parameter is not specified, the service files will be generated at the same location from which the GraphQL command is executed.                                                                                        |
| -m, --mode                    | The `mode` parameter specifies the operation mode. It can be `client`, `schema`, or `service`. They represent Ballerina GraphQL client generation, GraphQL schema generation, and Ballerina GraphQL service generation respectively. The `mode` flag is optional. If the `mode` flag is not specified, the `graphql` tool will infer the mode from the `input` file extension. |
| -r, --use-records-for-objects | The `use-records-for-objects` flag makes the GraphQL tool use record types for GraphQL object types whenever possible in Ballerina GraphQL service generation. This flag is optional and it can only be used with the GraphQL service generation.                                                                                                                                                                                          |

### Command for Ballerina GraphQL client generation [Experimental]

The `graphql` command in Ballerina can be used for GraphQL to Ballerina code generation and Ballerina to GraphQL schema generation. The command usages for GraphQL to Ballerina code generation are as follows.

```
bal graphql [-i | --input] <graphql-configuration-file-path> [-o | --output] <output-location>
```

The command line arguments below can be used with the command for each particular purpose as described below.
| Argument      | Description   |
| ------------- | ------------- |
| -i, --input   | The `input` parameter specifies the path of the GraphQL config file (e.g., graphql.config.yaml) configured with GraphQL schemas specified by Schema Definition Language and GraphQL documents. This parameter is mandatory.  |
| -o, --output  | The `output` parameter specifies the path of the output location of the generated files. This parameter is optional. If this parameter is not specified, the Ballerina files will be generated at the same location from which the GraphQL command is executed.  |

### Generate a Ballerina client from a GraphQL config file configured with a GraphQL schema (SDL) and GraphQL document [Experimental]

Create a GraphQL config file (graphql.config.yaml) with the following configuration.


```yml
schema: <File path to the GraphQL schema or the web URL of the GraphQL schema>
documents:
    - <File path to the GraphQL document with the GraphQL queries & mutations>
```

The client generated from the GraphQL config file can be used in your applications to call the queries/mutations in the GraphQL document against the GraphQL API corresponding to the GraphQL schema defined in the GraphQL config file.

If you want to generate the Ballerina client for a given GraphQL document you can use the following command.

```shell
bal graphql [-i | --input] <graphql-configuration-file-path> [-o | --output] <output-location>
```

This will generate a Ballerina client with remote operations corresponding to each GraphQL query/mutation in the GraphQL document (.graphql document).

For example,

```shell
bal graphql -i graphql.config.yaml
```

This will generate a Ballerina client stub (client.bal), a util file (utils.bal) for the relevant utils methods related to the client stub, a schema file (types.bal) for the configured GraphQL schema, and a config file (config_types.bal) for all the Ballerina data types related to connector configuration. The above command can be run from anywhere on the execution path. It is not mandatory to run it from within a Ballerina project.

> **_NOTE:_** If the GraphQL API contains an authentication mechanism, make sure to add the extensions section in the GraphQL config file with the relevant tokens and headers. In this scenario it’s mandatory to configure the schema section with the web URL of the GraphQL schema.

```yml
schema: <The web URL of the GraphQL schema.>
documents:
     - <File path to the GraphQL document with the GraphQL queries & mutations>
extensions:
     endpoints:
         default:
              headers: { "<Authorization>": "<Bearer token>", "<API_Header_Key1>": "<API_Header_Value1>", "<API_Header_Key2>": "<API_Header_Value2>" }

```

### Generate a Ballerina client from a GraphQL config file configured with a GraphQL schema (SDL) and multiple GraphQL documents [Experimental]

Create a GraphQL config file (graphql.config.yaml) with the following configuration.

```yml
schema: <File path to the GraphQL schema or the web URL of the GraphQL schema>
documents:
    - <File path to the GraphQL document with the GraphQL queries & mutations>
    - <File path to the GraphQL document with the GraphQL queries & mutations>
    - <File path to the GraphQL document with the GraphQL queries & mutations>
    - <Add more documents based on your requirement … >
```

The client generated from the GraphQL config file can be used in your applications to call the queries/mutations in each GraphQL document against the GraphQL API corresponding to the GraphQL schema defined in the GraphQL config file.

If you want to generate a single Ballerina client for a given set of GraphQL documents you can use the following command.

```shell
bal graphql [-i | --input] <graphql-configuration-file-path> [-o | --output] <output-location>
```

This will generate a Ballerina client to represent all  the GraphQL documents with remote operations corresponding to each GraphQL query/mutation in the GraphQL document (.graphql document).

For example,

```shell
bal graphql -i graphql.config.yaml
```

This will generate a Ballerina client stub (client.bal) with all the remote operations, a util file (utils.bal) for the relevant utils methods related to the client stub, a schema file (types.bal) for the configured GraphQL schema, and a config file (config_types.bal) for all the Ballerina data types related to connector configuration. The above command can be run from anywhere on the execution path. It is not mandatory to run it from within a Ballerina project.

### Generate multiple Ballerina modules from a GraphQL config file configured with multiple GraphQL projects

Create a GraphQL config file (graphql.config.yaml) with the following configuration.

```
projects:
    <project1_name>:
        schema: <File path to the GraphQL schema or the web URL of the GraphQL schema>
        documents:
            - <File path to the GraphQL document with the GraphQL queries & mutations>
            - <File path to the GraphQL document with the GraphQL queries & mutations>
            - <Add more documents based on your requirement … >
    <project2_name>:
        schema: <File path to the GraphQL schema or the web URL of the GraphQL schema>
        documents:
            - <File path to the GraphQL document with the GraphQL queries & mutations>
            - <File path to the GraphQL document with the GraphQL queries & mutations>
            - <Add more documents based on your requirement … >
    <project3_name>:
        schema: <File path to the GraphQL schema or the web URL of the GraphQL schema>
        documents:
            - <File path to the GraphQL document with the GraphQL queries & mutations>
            - <File path to the GraphQL document with the GraphQL queries & mutations>
            - <Add more documents based on your requirement … >
    <Add more projects based on your requirement … >
```

This enables you to work with multiple GraphQL APIs. Each GraphQL API should be defined under a separate project in the GraphQL config file. The client generated under a separate Ballerina module related to each project from the GraphQL config file can be used in your applications to call the queries/mutations in each GraphQL document against the GraphQL API corresponding to the GraphQL schema defined under each project in the GraphQL config file.

If you want to generate multiple Ballerina modules for a given set of GraphQL projects you can use the following command.

```
bal graphql [-i | --input] <graphql-configuration-file-path> [-o | --output] <output-location>
```

This will generate a separate Ballerina module for each GraphQL project with clients corresponding to each GraphQL document configured under each GraphQL project.

For example,
```
bal graphql -i graphql.config.yaml
```

This will generate a Ballerina module (project_name) corresponding to each GraphQL project. Each project will generate a Ballerina client stub (client.bal) corresponding to each GraphQL document configured under the relevant GraphQL project, an util file (utils.bal) for the relevant utils methods related to the client stubs, a schema file (types.bal) for the configured GraphQL schema under the relevant GraphQL project, and a config file (config_types.bal) for all the Ballerina data types related to connector configuration. The above command can be run from anywhere on the execution path. It is not mandatory to run it from within a Ballerina project.
