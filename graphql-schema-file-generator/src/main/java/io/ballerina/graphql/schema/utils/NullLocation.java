package io.ballerina.graphql.schema.utils;

import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;

/**
 * This {@code NullLocation} represents the null location allocation for scenarios which has no location.
 */
public final class NullLocation implements Location {

    private static NullLocation nullLocation = null;

    private NullLocation() {
    }

    public static NullLocation getInstance() {
        if (nullLocation == null) {
            nullLocation = new NullLocation();
        }
        return nullLocation;
    }

    @Override
    public LineRange lineRange() {
        LinePosition from = LinePosition.from(0, 0);
        return LineRange.from("", from, from);
    }

    @Override
    public TextRange textRange() {
        return TextRange.from(0, 0);
    }
}
