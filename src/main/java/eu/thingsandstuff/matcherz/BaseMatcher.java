package eu.thingsandstuff.matcherz;

import java.util.function.Predicate;

public class BaseMatcher<T> implements Matcher<T> {

    private final Class<T> target;
    private final Predicate<? super T> predicate;
    private Capture<? super T> capture;

    protected BaseMatcher(Class<T> target, Predicate<? super T> predicate) {
        this.target = target;
        this.predicate = predicate;
    }

    @Override
    public boolean matches(Object object) {
        return target.isInstance(object) && predicate.test(target.cast(object));
    }
}
