package rocks.matchmaker;

import java.util.List;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class Matcher<T> {

    public static Matcher<Object> any() {
        return match(Object.class);
    }

    public static <T> Matcher<T> match(Class<T> expectedClass) {
        return match(expectedClass, (x) -> true);
    }

    public static <T> Matcher<T> match(Class<T> targetClass, Predicate<T> predicate) {
        return match(Extractor.assumingType(targetClass, (x) -> Option.of(x).filter(predicate)));
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
    public static <F, T> Matcher<T> match(Extractor.Scoped<F, T> extractor) {
        return new Matcher<>(extractor.getScopeType(), extractor, emptyList(), null);
    }

    //TODO rethink having this method and the non-scoped extractor at all
    //(match selectiveness? strctural matching selectivity? performance?)
    public static <T> Matcher<T> match(Extractor<T> extractor) {
        return new Matcher<>(Object.class, extractor, emptyList(), null);
    }

    //scopeType unused for now, but will help in debugging and structural matching later
    private final Class<?> scopeType;
    private final Extractor<T> extractor;
    private final List<PropertyMatcher<T, ?>> propertyMatchers;
    private final Capture<T> capture;

    private Matcher(Class<?> scopeType, Extractor<T> extractor, List<PropertyMatcher<T, ?>> propertyMatchers, Capture<T> capture) {
        this.scopeType = scopeType;
        this.extractor = extractor;
        this.propertyMatchers = propertyMatchers;
        this.capture = capture;
    }

    public Matcher<T> capturedAs(Capture<T> capture) {
        if (this.capture != null) {
            throw new IllegalStateException("This matcher already has a capture alias");
        }
        return new Matcher<>(scopeType, extractor, propertyMatchers, capture);
    }

    public Matcher<T> matching(T value) {
        Class<T> scopeClass = (Class<T>) value.getClass();
        return matching(scopeClass, x -> x.equals(value));
    }

    public Matcher<T> matching(Class<T> scopeType, Predicate<T> predicate) {
        return matching(Extractor.assumingType(scopeType, x -> Option.of(x).filter(predicate)));
    }

    public Matcher<T> matching(Extractor.Scoped<?, T> extractor) {
        Matcher<T> matcher = match(extractor);
        return matching(matcher);
    }

    public <S> Matcher<T> matching(Matcher<S> matcher) {
        PropertyMatcher<T, S> selfMatcher = new PropertyMatcher<>(Option::of, matcher);
        return with(selfMatcher);
    }

    public Matcher<T> with(PropertyMatcher<? super T, ?> matcher) {
        PropertyMatcher<T, ?> castMatcher = contravariantUpcast(matcher);
        return new Matcher<>(scopeType, extractor, Util.append(propertyMatchers, castMatcher), capture);
    }

    //this reflects the fact that PropertyMatcher<F, T> is contravariant on F
    @SuppressWarnings("unchecked cast")
    private PropertyMatcher<T, ?> contravariantUpcast(PropertyMatcher<? super T, ?> matcher) {
        return (PropertyMatcher<T, ?>) matcher;
    }

    public Match<T> match(Object object) {
        Option<T> extractionResult = extractor.apply(object);
        return extractionResult
                .map(this::tryCreateMatch)
                .orElse(Match.empty());
    }

    private Match<T> tryCreateMatch(T matchedValue) {
        List<Match<?>> propertiesMatches = propertiesMatches(matchedValue);
        boolean allPropertiesMatch = propertiesMatches.stream().allMatch(Match::isPresent);
        return allPropertiesMatch ? combineCaptures(matchedValue, propertiesMatches) : Match.empty();
    }

    protected List<Match<?>> propertiesMatches(T matchedValue) {
        return propertyMatchers.stream().map(propertyMatcher -> {
            Option<?> propertyValueOption = propertyMatcher.getProperty().apply(matchedValue);
            return propertyValueOption
                    .map(value -> propertyMatcher.getMatcher().match(value))
                    .orElse(Match.empty());
        }).collect(toList());
    }

    private Match<T> combineCaptures(T matchedValue, List<Match<?>> propertyMatches) {
        Captures thisMatcherCaptures = Captures.ofNullable(capture, matchedValue);
        Captures allCaptures = propertyMatches.stream()
                .map(Match::captures)
                .reduce(thisMatcherCaptures, Captures::addAll);
        return Match.of(matchedValue, allCaptures);
    }
}
