package rocks.matchmaker;

import java.util.function.Function;

public class PropertyMatcher<T, S> {

    private final Function<T, Option<?>> property;
    private final Matcher<S> matcher;

    public PropertyMatcher(Function<T, Option<?>> property, Matcher<S> matcher) {
        this.property = property;
        this.matcher = matcher;
    }

    public Function<T, Option<?>> getProperty() {
        return property;
    }

    public Matcher<S> getMatcher() {
        return matcher;
    }
}
