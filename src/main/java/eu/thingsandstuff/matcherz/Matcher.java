package eu.thingsandstuff.matcherz;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public class Matcher<T> {

    static Matcher<Object> any() {
        return match(Object.class);
    }

    static <T> Matcher<T> match(Class<T> expectedClass) {
        return match(expectedClass, (x) -> true);
    }

    static <T> Matcher<T> match(Class<T> targetClass, Predicate<T> predicate) {
        return match(Extractor.assuming(targetClass, (x) -> Match.of(x).filter(predicate)));
    }

    /**
     * For cases when evaluating a property is needed to check
     * if it's possible to construct an object and that object's
     * construction largely repeats checking the property.
     * <p>
     * E.g. let's say we have a set and we'd like to match
     * other sets having a non-empty intersection with it.
     * If the intersection is not empty, we'd like to use it
     * in further computations. Extractors allow for exactly that.
     * <p>
     * An adequate extractor for the example above would compute
     * the intersection and return it wrapped in a Match
     * (think: null-capable Optional with a field for storing captures).
     * If the intersection would be empty, the extractor should
     * would a non-match by returning Match.empty().
     *
     * @param extractor
     * @param <T>       type of the extracted value
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

    public Match<T> match(Object object) {
        Match<T> match = addCapture(extractor.apply(object));
        return match
                .map(matchedValue -> propertyMatchers.stream().collect(toMap(identity(), pm -> {
                    Object propertyValue = pm.getProperty().apply(matchedValue);
                    return pm.getMatcher().match(propertyValue);
                })))
                .filter(propertyMatches -> propertyMatches.values().stream().allMatch(Match::isPresent))
                .flatMap(propertyMatches -> addAll(match, nestedCapures(propertyMatches)));

    }

    private Match<T> addAll(Match<T> match, Stream<Map.Entry<Capture<?>, Object>> captures) {
        Iterator<Map.Entry<Capture<?>, Object>> iterator = captures.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Capture<?>, Object> capture = iterator.next();
            match = match.withCapture(capture.getKey(), capture.getValue());
        }
        return match;
    }

    private Stream<Map.Entry<Capture<?>, Object>> nestedCapures(Map<PropertyMatcher<T, ?>, ? extends Match<?>> propertyMatches) {
        return propertyMatches.values().stream().flatMap(
                pm -> pm.captures().entrySet().stream()
        );
    }

    private Match<T> addCapture(Match<T> match) {
        return match.isPresent() && capture != null ? match.withCapture(capture, match.value()) : match;
    }

}
