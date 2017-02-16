package rocks.matchmaker;

import java.util.function.Function;

public class PropertyMatcher<T, S> {

    private final Function<T, Option<?>> property;
    private final Matcher<S> matcher;

    private PropertyMatcher(Function<T, Option<?>> property, Matcher<S> matcher) {
        this.property = property;
        this.matcher = matcher;
    }

    //this reflects the fact that PropertyMatcher<F, T> is contravariant on F and covaraint on T
    @SuppressWarnings("unchecked cast")
    public static <T, R> PropertyMatcher<T, R> upcast(PropertyMatcher<? super T, ? extends R> matcher) {
        return (PropertyMatcher<T, R>) matcher;
    }

    public Function<T, Option<?>> getProperty() {
        return property;
    }

    public Matcher<S> getMatcher() {
        return matcher;
    }

    public static <F, T, R> PropertyMatcher<F, R> of(Function<F, Option<T>> property, Matcher<R> matcher) {
        //without the ::apply below, the type system is unable to drop the T type from Option
        return new PropertyMatcher<>(property::apply, matcher);
    }
}
