package rocks.matchmaker;

import java.util.function.Function;

public interface Extractor<T> extends Function<Object, Option<T>> {

    static <T, S> Extractor<S> assumingType(Class<T> type, Function<T, Option<S>> extractor) {
        return (x) -> type.isInstance(x) ?
                extractor.apply(type.cast(x)) :
                Option.empty();
    }
}
