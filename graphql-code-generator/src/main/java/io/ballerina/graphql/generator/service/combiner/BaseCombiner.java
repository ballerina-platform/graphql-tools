package io.ballerina.graphql.generator.service.combiner;

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.formatter.core.FormatterException;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for combining the generated code.
 */
public abstract class BaseCombiner {
    private final ModulePartNode nextContentNode;
    private final ModulePartNode prevContentNode;
    private List<String> breakingChangeWarnings;

    public BaseCombiner(ModulePartNode prevContentNode, ModulePartNode nextContentNode) {
        this.prevContentNode = prevContentNode;
        this.nextContentNode = nextContentNode;

        breakingChangeWarnings = new ArrayList<>();
    }

    public abstract String generateMergedSrc() throws FormatterException;

    public abstract SyntaxTree generateMergedSyntaxTree();

    public List<String> getBreakingChangeWarnings() {
        return breakingChangeWarnings;
    }
}
