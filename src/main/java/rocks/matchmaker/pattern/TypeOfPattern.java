package rocks.matchmaker.pattern;

import rocks.matchmaker.Pattern;

public class TypeOfPattern<T> extends Pattern<T> {

    private final Class<T> expectedClass;

    public TypeOfPattern(Class<T> expectedClass) {
        this.expectedClass = expectedClass;
    }

    public Class<T> expectedClass() {
        return expectedClass;
    }
}
