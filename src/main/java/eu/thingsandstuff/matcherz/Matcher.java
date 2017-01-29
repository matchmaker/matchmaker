package eu.thingsandstuff.matcherz;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;

public class Matcher<T> {

    static Matcher<Object> any() {
        return match(Object.class);
    }

    static <T> Matcher<T> match(Class<T> expectedClass) {
        return match(expectedClass, (x) -> true);
    }

    static <T> Matcher<T> match(Class<T> targetClass, Predicate<T> predicate) {
        return match(Extractor.assuming(targetClass, (x) -> Optional.of(x).filter(predicate)));
    }

    /**
     * For cases when evaluating a property is needed to check
     * if it's possible to construct an object and that object's
     * construction largely repeats checking the property.
     *
     * E.g. let's say we have a set and we'd like to match
     * other sets having a non-empty intersection with it.
     * If the intersection is not empty, we'd like to use it
     * in further computations. Extractors allow for exactly that.
     *
     * An adequate extractor for the example above would compute
     * the intersection and return it wrapped in an Optional.
     * If the intersection would be empty, the extractor should
     * would a non-match by returning Optional.empty().
     *
     * @param extractor
     * @param <T> type of the extracted value
     * @return
     */
    static <T> Matcher<T> match(Extractor<T> extractor) {
        return new Matcher<>(extractor, emptyList(), null);
    }

    private final Extractor<T> extractor;
    private final List<PropertyMatcher<T, ?>> propertyMatchers;
    private final Capture<T> capture;

    private Matcher(Extractor<T> extractor, List<PropertyMatcher<T, ?>> propertyMatchers, Capture<T> capture) {
        this.extractor = extractor;
        this.propertyMatchers = propertyMatchers;
        this.capture = capture;
    }

    public Matcher<T> as(Capture<T> capture) {
        if (this.capture != null) {
            throw new IllegalStateException("This matcher already has a capture alias");
        }
        return new Matcher<>(extractor, propertyMatchers, capture);
    }

    @SuppressWarnings("unchecked cast")
    public Matcher<T> with(PropertyMatcher<T, ?> matcher) {
        return new Matcher<>(extractor, Util.append(propertyMatchers, matcher), capture);
    }

    public Optional<T> match(Object object) {
        Predicate<T> propertiesMatch = (matchedValue) -> propertyMatchers.stream().allMatch(pm -> {
            Object propertyValue = pm.getProperty().apply(matchedValue);
            return pm.getMatcher().match(propertyValue).isPresent();
        });
        return extractor.apply(object).filter(propertiesMatch);
    }

}
