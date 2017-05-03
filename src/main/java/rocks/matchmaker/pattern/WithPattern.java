package rocks.matchmaker.pattern;

import rocks.matchmaker.Option;
import rocks.matchmaker.Pattern;
import rocks.matchmaker.PropertyPattern;

import java.util.function.Function;

public class WithPattern<T> extends Pattern<T> {

    private final PropertyPattern<? super T, ?> propertyPattern;

    public WithPattern(PropertyPattern<? super T, ?> propertyPattern, Pattern<T> previous) {
        super(previous);
        this.propertyPattern = propertyPattern;
    }

    public Pattern<?> getPattern() {
        return propertyPattern.getPattern();
    }

    public Function<? super T, Option<?>> getProperty() {
        return propertyPattern.getProperty();
    }
}
