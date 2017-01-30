package rocks.matchmaker;

import java.util.function.Function;

public interface Extractor<T> extends Function<Object, Match<T>> {

    static <T, S> Extractor<S> assumingType(Class<T> type, Function<T, Match<S>> extractor) {
        return (x) -> type.isInstance(x) ?
                extractor.apply(type.cast(x)) :
                Match.empty();
    }
}
