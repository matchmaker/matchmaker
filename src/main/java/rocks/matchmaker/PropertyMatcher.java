package rocks.matchmaker;

import java.util.function.Function;

public class PropertyMatcher<F, R> {

    private final Function<F, Option<?>> property;
    private final Pattern<R> pattern;

    //this reflects the fact that PropertyMatcher<F, R> is contravariant on F and covaraint on R
    @SuppressWarnings("unchecked cast")
    public static <F, R> PropertyMatcher<F, R> upcast(PropertyMatcher<? super F, ? extends R> matcher) {
        return (PropertyMatcher<F, R>) matcher;
    }

    public static <F, T, R> PropertyMatcher<F, R> of(Function<F, Option<T>> property, Pattern<R> pattern) {
        //without the ::apply below, the type system is unable to drop the R type from Option
        return new PropertyMatcher<>(property::apply, pattern);
    }

    private PropertyMatcher(Function<F, Option<?>> property, Pattern<R> pattern) {
        this.property = property;
        this.pattern = pattern;
    }

    public Function<F, Option<?>> getProperty() {
        return property;
    }

    public Pattern<R> getPattern() {
        return pattern;
    }
}
