package rocks.matchmaker;

import java.util.function.Predicate;

public interface Predicates {

    static <T> Predicate<Object> assumingType(Class<T> type, Predicate<T> predicate) {
        return (x) -> (type.isInstance(x)) && predicate.test(type.cast(x));
    }
}
