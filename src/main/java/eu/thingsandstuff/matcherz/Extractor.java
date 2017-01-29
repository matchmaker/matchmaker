package eu.thingsandstuff.matcherz;

import java.util.Optional;
import java.util.function.Function;

public interface Extractor<T> extends Function<Object, Optional<T>> {

    static <T, S> Extractor<S> assuming(Class<T> targetClass, Function<T, Optional<S>> predicate) {
        return (x) -> targetClass.isInstance(x) ?
                predicate.apply(targetClass.cast(x)) :
                Optional.empty();
    }
}
