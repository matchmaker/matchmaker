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
            public <R> PropertyPattern<F, R> matching(Pattern<R> pattern) {
                return PropertyPattern.of(property, pattern);
            }
        };
    }

    static <T> Property<T, T> self() {
        return property(Function.identity());
    }

    default PropertyPattern<F, T> capturedAs(Capture<T> capture) {
        Pattern<T> matchAll = (Pattern<T>) Pattern.any();
        return matching(matchAll.capturedAs(capture));
    }

    default PropertyPattern<F, T> equalTo(T value) {
        return matching(Pattern.equalTo(value));
    }

    default PropertyPattern<F, T> ofType(Class<? extends T> type) {
        return matching(Pattern.upcast(Pattern.typeOf(type)));
    }

    @SuppressWarnings("unchecked cast")
    //the `matchAll` matcher will only ever be passed the return values matching
    //the `property` function.
    default PropertyPattern<F, T> matching(Predicate<? super T> predicate) {
        Pattern<T> matchAll = (Pattern<T>) Pattern.any();
        return matching(matchAll.matching(predicate));
    }

    @SuppressWarnings("unchecked cast")
    //the `matchAll` matcher will only ever be passed the return values matching
    //the `property` function.
    default <R> PropertyPattern<F, R> matching(Extractor<T, R> extractor) {
        Pattern<T> matchAll = (Pattern<T>) Pattern.any();
        return matching(matchAll.matching(extractor));
    }

    <R> PropertyPattern<F, R> matching(Pattern<R> pattern);
}
