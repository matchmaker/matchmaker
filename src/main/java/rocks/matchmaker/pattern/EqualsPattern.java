package rocks.matchmaker.pattern;

import rocks.matchmaker.Pattern;

public class EqualsPattern<T> extends Pattern<T> {

    private final T expectedValue;

    public EqualsPattern(T expectedValue) {
        this.expectedValue = expectedValue;
    }

    public T expectedValue() {
        return expectedValue;
    }
}
