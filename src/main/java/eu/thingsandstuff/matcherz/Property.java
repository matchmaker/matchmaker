package eu.thingsandstuff.matcherz;

import java.util.function.Function;

interface Property<T, S> {

    static <T, S> Property<T, S> $(Function<T, S> f) {
        return new Property<T, S>() {
            @Override
            public <S1> PropertyMatcher<T, S1> matching(Matcher<? super S1> matcher) {
                return new PropertyMatcher<T, S1>() {
                };
            }
        };
    }

    <S> PropertyMatcher<T, S> matching(Matcher<? super S> matcher);
}