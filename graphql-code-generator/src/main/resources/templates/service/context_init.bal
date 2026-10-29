isolated function initContext(http:RequestContext requestContext, http:Request request) returns graphql:Context|error {
graphql:Context context = new;
{{registrations}}
return context;
}
