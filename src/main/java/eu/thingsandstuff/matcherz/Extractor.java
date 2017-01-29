package eu.thingsandstuff.matcherz;

import java.util.function.Function;

public interface Extractor<T> extends Function<Object, Match<T>> {

    static <T, S> Extractor<S> assuming(Class<T> targetClass, Function<T, Match<S>> extractor) {
        return (x) -> targetClass.isInstance(x) ?
                extractor.apply(targetClass.cast(x)) :
                Match.empty();
    }
}
