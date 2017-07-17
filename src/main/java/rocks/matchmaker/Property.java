package rocks.matchmaker;

import rocks.matchmaker.pattern.ExtractPattern;
import rocks.matchmaker.pattern.FilterPattern;

import java.util.function.Function;
import java.util.function.Predicate;

public interface Property<F, T> {

    <R> PropertyPattern<F, R> matching(Pattern<R> pattern);

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
        //FIXME equals is a transforming pattern
        return matching(Pattern.equalTo(value));
    }

    default PropertyPattern<F, T> ofType(Class<? extends T> type) {
        return matching(Pattern.upcast(Pattern.typeOf(type)));
    }

    //FIXME add 'with' shortcut/sugar? Or not, b/c dsl formatting? Document reason?
    default PropertyPattern<F, T> matching(Predicate<? super T> predicate) {
        return matching(new FilterPattern<>(predicate, null));
    }

    default <R> PropertyPattern<F, R> matching(Extractor<T, R> extractor) {
        //FIXME what does this tell me about the 'previous' field? pattern composability? seeds and transforms?
        return matching(new ExtractPattern<>(extractor, null));
    }

}
