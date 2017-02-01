package rocks.matchmaker;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface Extractor<T> extends BiFunction<Object, Captures, Option<T>> {

    static <T, S> Extractor.Scoped<T, S> assumingType(Class<T> type, Function<T, Option<S>> extractor) {
        return new Scoped<>(type, (matchedObject, captures) -> extractor.apply(matchedObject));
    }

    static <T, S> Extractor.Scoped<T, S> assumingType(Class<T> type, BiFunction<T, Captures, Option<S>> extractor) {
        return new Scoped<>(type, extractor);
    }

    class Scoped<T, S> implements Extractor<S> {

        private final Class<T> scopeType;
        private final BiFunction<T, Captures, Option<S>> extractor;

        private Scoped(Class<T> scopeType, BiFunction<T, Captures, Option<S>> extractor) {
            this.scopeType = scopeType;
            this.extractor = extractor;
        }

        public Class<T> getScopeType() {
            return scopeType;
        }

        @Override
        public Option<S> apply(Object x, Captures captures) {
            return scopeType.isInstance(x) ?
                    extractor.apply(scopeType.cast(x), captures) :
                    Option.empty();
        }
    }
}
