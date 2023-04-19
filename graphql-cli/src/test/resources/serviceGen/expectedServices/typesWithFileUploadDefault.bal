import ballerina/graphql;

type SchemaWithFileUploadApi service object {
    *graphql:Service;

    resource function get getUploadedFileNames() returns string[];
    remote function fileUpload(graphql:Upload file) returns string;
};
