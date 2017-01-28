package eu.thingsandstuff.matcherz;

import java.util.function.Predicate;

@FunctionalInterface
public interface Matcher<T> {

    static <T> Matcher<T> match(Class<T> expectedClass) {
        return match(expectedClass, expectedClass::isInstance);
    }

    static <T> Matcher<T> match(Class<T> targetClass, Predicate<T> predicate) {
        return new BaseMatcher<T>(targetClass, predicate);
    }

    static <T> Matcher<T> any() {
        return object -> true;
    }


    default Matcher<T> as(Capture<? super T> capture) {
        return this;
    }

    default <S> Matcher<T> with(PropertyMatcher<? extends T, S> matcher) {
        return this;
    }


    boolean matches(Object object);
}
