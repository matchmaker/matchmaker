package rocks.matchmaker;

import java.util.function.Function;
import java.util.function.Predicate;

import static rocks.matchmaker.Matcher.$;

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

    //TODO rename back to the same name as other property refiners and make sure there are no signature abimguities
    default <S> PropertyMatcher<F, S> equalTo(S value) {
        return matching(Matcher.equalTo(value));
    }

    //TODO make Property carry the scopeType and remove scopeType from methods below?
    default <S> PropertyMatcher<F, S> matching(Class<S> scopeType, Predicate<S> predicate) {
        return matching($(scopeType).$(predicate));
    }

    //FIXME introduce the third type to PropertyMatcher
    //FIXME restore exrtractor-matching ability for properties (?)
//    default <S> PropertyMatcher<F, S> matching(Extractor.Scoped<F, S> extractor) {
//        return matching($(extractor.getScopeType()).matching(extractor));
//    }

    <S> PropertyMatcher<F, S> matching(Matcher<S> matcher);
}
