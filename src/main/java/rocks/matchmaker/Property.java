package rocks.matchmaker;

import java.util.function.Function;

public interface Property<T> {

    <S> PropertyMatcher<T, S> matching(Matcher<S> matcher);

    static <T> Property<T> property(Function<T, ?> property) {
        return new Property<T>() {
            @Override
            public <S> PropertyMatcher<T, S> matching(Matcher<S> matcher) {
                return new PropertyMatcher<>(property, matcher);
            }
        };
    }
}