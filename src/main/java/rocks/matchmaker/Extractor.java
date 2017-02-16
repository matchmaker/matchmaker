package rocks.matchmaker;

import java.util.function.BiFunction;

public interface Extractor<F, T> extends BiFunction<F, Captures, Option<T>> {

}
