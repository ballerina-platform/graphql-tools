import ballerina/graphql;

type SchemaWithFileUploadApiService service object {
    *graphql:Service;
    resource function get getUploadedFileNames() returns string[]|error;
    remote function fileUpload(graphql:Upload file) returns string|error;
};
