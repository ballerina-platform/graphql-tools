{{imports}}

configurable int port = 9090;{{contextInit}}

{{serviceConfig}}service {{name}} on new graphql:Listener(port) {
{{resolvers}}
}{{batchFunctions}}
