package eu.thingsandstuff.matcherz;

import java.util.function.Predicate;

public class BaseMatcher<T> implements Matcher<T> {

    private final Predicate<T> predicate;
    private Capture<? super T> capture;

    protected BaseMatcher(Predicate<T> predicate) {
        this.predicate = predicate;
    }

    @Override
    public Matcher<T> as(Capture<? super T> capture) {
        this.capture = capture;
        return this;
    }

    @Override
    public <S> Matcher<T> with(PropertyMatcher<? extends T, S> matcher) {
        return this;
    }
}
