package rocks.matchmaker;

import java.util.function.Function;
import java.util.function.Predicate;

import static rocks.matchmaker.Matcher.matcher;

public interface Property<T> {

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

    default <S> PropertyMatcher<T, S> matching(S value) {
        return matching(Matcher.equalTo(value));
    }

    default <S> PropertyMatcher<T, S> matching(Class<S> scopeType, Predicate<S> predicate) {
        return matching(matcher(scopeType, predicate));
    }

    default <S> PropertyMatcher<T, S> matching(Extractor.Scoped<?, S> extractor) {
        return matching(matcher(extractor));
    }

    <S> PropertyMatcher<T, S> matching(Matcher<S> matcher);
}