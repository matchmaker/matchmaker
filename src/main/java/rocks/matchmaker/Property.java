package rocks.matchmaker;

import java.util.function.Function;

public interface Property<T> {

    static <T> Property<T> $(Function<T, ?> property) {
        return new Property<T>() {
            @SuppressWarnings("unchecked cast")
            @Override
            public <S> PropertyMatcher<T, S> matching(Matcher<?> matcher) {
                Function<T, S> propertyCast = (Function<T, S>) property;
                Matcher<S> matcherCast = (Matcher<S>) matcher;
                return new PropertyMatcher<>(propertyCast, matcherCast);
            }
        };
    }

    <S> PropertyMatcher<T, S> matching(Matcher<? extends Object> matcher);
}