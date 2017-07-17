package rocks.matchmaker;

import java.util.function.Function;

public class PropertyPattern<F, R> {

    private final Function<F, Option<?>> property;
    private final Pattern<R> pattern;

    //this reflects the fact that PropertyPattern<F, R> is contravariant on F and covaraint on R
    @SuppressWarnings("unchecked cast")
    public static <F, R> PropertyPattern<F, R> upcast(PropertyPattern<? super F, ? extends R> pattern) {
        return (PropertyPattern<F, R>) pattern;
    }

    public static <F, T, R> PropertyPattern<F, R> of(Function<F, Option<T>> property, Pattern<R> pattern) {
        //FIXME check state pattern has no previous?
        //FIXME or maybe 'DereferenceProperty' is the previous pattern?
        //without the ::apply below, the type system is unable to drop the R type from Option
        return new PropertyPattern<>(property::apply, pattern);
    }

    private PropertyPattern(Function<F, Option<?>> property, Pattern<R> pattern) {
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
