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
            public <R> PropertyMatcher<F, R> matching(Pattern<R> pattern) {
                return PropertyMatcher.of(property, pattern);
            }
        };
    }

    static <T> Property<T, T> self() {
        return property(Function.identity());
    }

    default PropertyMatcher<F, T> capturedAs(Capture<T> capture) {
        Pattern<T> matchAll = (Pattern<T>) Pattern.any();
        return matching(matchAll.capturedAs(capture));
    }

    default PropertyMatcher<F, T> equalTo(T value) {
        return matching(Pattern.equalTo(value));
    }

    default PropertyMatcher<F, T> ofType(Class<? extends T> type) {
        return matching(Pattern.upcast(Pattern.typeOf(type)));
    }

    @SuppressWarnings("unchecked cast")
    //the `matchAll` matcher will only ever be passed the return values matching
    //the `property` function.
    default PropertyMatcher<F, T> matching(Predicate<? super T> predicate) {
        Pattern<T> matchAll = (Pattern<T>) Pattern.any();
        return matching(matchAll.matching(predicate));
    }

    @SuppressWarnings("unchecked cast")
    //the `matchAll` matcher will only ever be passed the return values matching
    //the `property` function.
    default <R> PropertyMatcher<F, R> matching(Extractor<T, R> extractor) {
        Pattern<T> matchAll = (Pattern<T>) Pattern.any();
        return matching(matchAll.matching(extractor));
    }

    <R> PropertyMatcher<F, R> matching(Pattern<R> pattern);
}
