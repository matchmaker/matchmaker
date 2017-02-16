package rocks.matchmaker;

import java.util.function.Function;

public class PropertyMatcher<F, R> {

    private final Function<F, Option<?>> property;
    private final Matcher<R> matcher;

    private PropertyMatcher(Function<F, Option<?>> property, Matcher<R> matcher) {
        this.property = property;
        this.matcher = matcher;
    }

    //this reflects the fact that PropertyMatcher<F, R> is contravariant on F and covaraint on R
    @SuppressWarnings("unchecked cast")
    public static <F, R> PropertyMatcher<F, R> upcast(PropertyMatcher<? super F, ? extends R> matcher) {
        return (PropertyMatcher<F, R>) matcher;
    }

    public Function<F, Option<?>> getProperty() {
        return property;
    }

    public Matcher<R> getMatcher() {
        return matcher;
    }

    public static <F, T, R> PropertyMatcher<F, R> of(Function<F, Option<T>> property, Matcher<R> matcher) {
        //without the ::apply below, the type system is unable to drop the R type from Option
        return new PropertyMatcher<>(property::apply, matcher);
    }
}
