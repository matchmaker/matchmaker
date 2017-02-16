package rocks.matchmaker;

import java.util.function.Function;
import java.util.function.Predicate;

import static rocks.matchmaker.Matcher.$;

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

    //TODO rename back to the same name as other property refiners and make sure there are no signature abimguities
    default <S> PropertyMatcher<T, S> equalTo(S value) {
        return matching(Matcher.equalTo(value));
    }

    //TODO make Property carry the scopeType and remove scopeType from methods below?
    default <S> PropertyMatcher<T, S> matching(Class<S> scopeType, Predicate<S> predicate) {
        return matching($(scopeType).$(predicate));
    }

    //FIXME restore exrtractor-matching ability for properties (?)
//    default <S> PropertyMatcher<T, S> matching(Extractor.Scoped<T, S> extractor) {
//        return matching($(extractor.getScopeType()).matching(extractor));
//    }

    <S> PropertyMatcher<T, S> matching(Matcher<S> matcher);
}
