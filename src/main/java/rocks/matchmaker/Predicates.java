package rocks.matchmaker;

import java.util.function.Predicate;

public interface Predicates {

    static <T> Predicate<Object> assumingType(Class<T> targetClass, Predicate<T> predicate) {
        return (x) -> (targetClass.isInstance(x)) && predicate.test(targetClass.cast(x));
    }
}
