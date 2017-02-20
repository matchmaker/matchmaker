package rocks.matchmaker;

import java.util.function.Function;
import java.util.function.Predicate;

public interface Property<F, T> {

    static <F, T> Property<F, T> property(Function<F, T> property) {
        return optionalProperty(source -> Option.of(property.apply(source)));
    }

    static <F, T> Property<F, T> optionalProperty(Function<F, Option<T>> property) {
        return new Property<F, T>() {
            @Override
            public <R> PropertyMatcher<F, R> matching(Matcher<R> matcher) {
                return PropertyMatcher.of(property, matcher);
            }
        };
    }

    static <T> Property<T, T> self() {
        return property(Function.identity());
    }

    default PropertyMatcher<F, T> capturedAs(Capture<T> capture) {
        Matcher<T> matchAll = (Matcher<T>) Matcher.any();
        return matching(matchAll.capturedAs(capture));
    }

    default PropertyMatcher<F, T> equalTo(T value) {
        return matching(Matcher.equalTo(value));
    }

    default PropertyMatcher<F, T> ofType(Class<? extends T> type) {
        return matching(Matcher.upcast(Matcher.typeOf(type)));
    }

    @SuppressWarnings("unchecked cast")
    //the `matchAll` matcher will only ever be passed the return values matching
    //the `property` function.
    default PropertyMatcher<F, T> matching(Predicate<? super T> predicate) {
        Matcher<T> matchAll = (Matcher<T>) Matcher.any();
        return matching(matchAll.matching(predicate));
    }

    @SuppressWarnings("unchecked cast")
    //the `matchAll` matcher will only ever be passed the return values matching
    //the `property` function.
    default <R> PropertyMatcher<F, R> matching(Extractor<T, R> extractor) {
        Matcher<T> matchAll = (Matcher<T>) Matcher.any();
        return matching(matchAll.matching(extractor));
    }

    <R> PropertyMatcher<F, R> matching(Matcher<R> matcher);
}
