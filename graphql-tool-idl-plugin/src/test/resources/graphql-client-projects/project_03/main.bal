client "./graphql.config.yaml" as foo;

public function main() {
    client "./graphql.config.yaml" as bar;
    foo:client x;
    bar:client y;
}