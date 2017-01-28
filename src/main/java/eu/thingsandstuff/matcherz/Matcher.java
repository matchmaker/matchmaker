package eu.thingsandstuff.matcherz;

import java.util.List;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;

public class Matcher<T> {

    static Matcher<Object> any() {
        return match(Object.class, (x) -> true);
    }

    static <T> Matcher<T> match(Class<T> expectedClass) {
        return match(expectedClass, expectedClass::isInstance);
    }

    static <T> Matcher<T> match(Class<T> targetClass, Predicate<T> predicate) {
        return new Matcher<>(targetClass, predicate, emptyList(), null);
    }

    private final Class<T> target;
    private final Predicate<? super T> predicate;
    private final List<PropertyMatcher<T, ?>> propertyMatchers;
    private final Capture<? super T> capture;

    private Matcher(Class<T> target, Predicate<? super T> predicate, List<PropertyMatcher<T, ?>> propertyMatchers, Capture<? super T> capture) {
        this.target = target;
        this.predicate = predicate;
        this.propertyMatchers = propertyMatchers;
        this.capture = capture;
    }

    public Matcher<T> as(Capture<? super T> capture) {
        if (this.capture != null) {
            throw new IllegalStateException("This matcher already has a capture alias");
        }
        return new Matcher<>(target, predicate, propertyMatchers, capture);
    }

    @SuppressWarnings("unchecked cast")
    public Matcher<T> with(PropertyMatcher<? super T, ?> matcher) {
        PropertyMatcher<T, ?> newMatcher = (PropertyMatcher<T, ?>) matcher;
        return new Matcher<>(target, predicate, Util.append(propertyMatchers, newMatcher), capture);
    }

    public boolean matches(Object object) {
        if (object == null) {
            return predicate.test(target.cast(object));
        } else if (!target.isInstance(object)) {
            return false;
        } else {
            T matchedValue = target.cast(object);
            return predicate.test(matchedValue) && propertyMatchers.stream().allMatch(pm -> {
                Object propertyValue = pm.getProperty().apply(matchedValue);
                return pm.getMatcher().matches(propertyValue);
            });
        }
    }
}
