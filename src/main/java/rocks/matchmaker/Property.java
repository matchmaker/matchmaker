package rocks.matchmaker;

import java.util.function.Function;

public interface Property<T> {

    <S> PropertyMatcher<T, S> matching(Matcher<S> matcher);

    static <T> Property<T> property(Function<T, ?> property) {
        return optionalProperty(source -> Option.of(property.apply(source)));
    }

    static <T> Property<T> optionalProperty(Function<T, Option<?>> property) {
        return new Property<T>() {
            @Override
            public <S> PropertyMatcher<T, S> matching(Matcher<S> matcher) {
                return new PropertyMatcher<>(property, matcher);
            }
        };
    }

    static <T> Property<T> self() {
        return property(Function.identity());
    }
}