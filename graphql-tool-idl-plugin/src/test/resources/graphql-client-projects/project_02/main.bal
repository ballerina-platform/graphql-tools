client "./graphql.config.yaml" as foo;
client "./graphql.config.yaml" as bar;

public function main() {
    foo:client x;
    bar:client y;
}
