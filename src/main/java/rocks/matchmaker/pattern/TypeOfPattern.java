package rocks.matchmaker.pattern;

import rocks.matchmaker.Pattern;

public class TypeOfPattern<T> extends Pattern<T> {

    private final Class<T> expectedClass;

    public TypeOfPattern(Class<T> expectedClass) {
        super(expectedClass, null); //FIXME this is only until we have a real interpreter for pattern matching
        this.expectedClass = expectedClass;
    }

    public Class<T> expectedClass() {
        return expectedClass;
    }
}
