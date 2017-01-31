package rocks.matchmaker;

import java.util.function.Function;

public interface Extractor<T> extends Function<Object, Option<T>> {

    static <T, S> Extractor.Scoped<T, S> assumingType(Class<T> type, Function<T, Option<S>> extractor) {
        return new Scoped<>(type, extractor);
    }

    class Scoped<T, S> implements Extractor<S> {

        private final Class<T> scopeType;
        private final Function<T, Option<S>> extractor;

        private Scoped(Class<T> scopeType, Function<T, Option<S>> extractor) {
            this.scopeType = scopeType;
            this.extractor = extractor;
        }

        public Class<T> getScopeType() {
            return scopeType;
        }

        @Override
        public Option<S> apply(Object x) {
            return scopeType.isInstance(x) ?
                    extractor.apply(scopeType.cast(x)) :
                    Option.empty();
        }
    }
}
