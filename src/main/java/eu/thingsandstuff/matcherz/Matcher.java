package eu.thingsandstuff.matcherz;

import java.util.function.Predicate;

public interface Matcher<T> {

    static <T> Matcher<T> of(Class<T> expectedClass) {
        return of(expectedClass::isInstance);
    }

    static <T> Matcher<T> of(Predicate<T> predicate) {
        return new BaseMatcher<T>(predicate);
    }

    static <T> Matcher<T> any() {
        return of((x) -> true);
    }

    Matcher<T> as(Capture<? super T> capture);

    <S> Matcher<T> with(PropertyMatcher<? extends T, S> matcher);
}
