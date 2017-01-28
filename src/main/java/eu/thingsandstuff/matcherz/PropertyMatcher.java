package eu.thingsandstuff.matcherz;

import java.util.function.Function;

public class PropertyMatcher<T, S> {

    private final Function<T, S> property;
    private final Matcher<? super S> matcher;

    public PropertyMatcher(Function<T, S> property, Matcher<S> matcher) {
        this.property = property;
        this.matcher = matcher;
    }

    public Function<T, S> getProperty() {
        return property;
    }

    public Matcher<? super S> getMatcher() {
        return matcher;
    }
}
