package rocks.matchmaker;

import rocks.matchmaker.matching.DefaultMatchingStrategy;

import java.util.List;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;

public class Matcher<T> {

    public static Matcher<Object> any() {
        return matcher(Object.class);
    }

    public static <T> Matcher<T> equalTo(T expectedValue) {
        Class<T> expectedClass = (Class<T>) expectedValue.getClass();
        return matcher(expectedClass, (x) -> x.equals(expectedValue));
    }

    public static <T> Matcher<T> matcher(Class<T> expectedClass) {
        return matcher(expectedClass, (x) -> true);
    }

    public static <T> Matcher<T> matcher(Class<T> targetClass, Predicate<T> predicate) {
        return matcher(Extractor.assumingType(targetClass, (x) -> Option.of(x).filter(predicate)));
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
    public static <F, T> Matcher<T> matcher(Extractor.Scoped<F, T> extractor) {
        return new Matcher<>(extractor.getScopeType(), extractor, emptyList(), null);
    }

    //TODO rethink having this method and the non-scoped extractor at all
    //(match selectiveness? strctural matching selectivity? performance?)
    public static <T> Matcher<T> matcher(Extractor<T> extractor) {
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
        return matching(equalTo(value));
    }

    public Matcher<T> matching(Class<T> scopeType, Predicate<T> predicate) {
        return matching(matcher(scopeType, predicate));
    }

    public Matcher<T> matching(Extractor.Scoped<?, T> extractor) {
        return matching(matcher(extractor));
    }

    public <S> Matcher<T> matching(Matcher<S> matcher) {
        PropertyMatcher<T, S> selfPropertyMatcher = new PropertyMatcher<>(Option::of, matcher);
        return with(selfPropertyMatcher);
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
        return match(object, Captures.empty());
    }

    public Match<T> match(Object object, Captures captures) {
        return match(object, captures, DefaultMatchingStrategy.INSTANCE);
    }

    public Match<T> match(Object object, Captures captures, MatchingStrategy matchingStrategy) {
        return matchingStrategy.match(this, object, captures);
    }

    /**
     * This method produces an Internals token used to access
     * internal properties of Matchers. The purpose of this method
     * is to warn users that any method accepting the token and
     * any values received from token-accepting methods is subject
     * to change in future versions.
     *
     * @return Matcher.Internals access token
     */
    public static Internals internals() {
        return Internals.INSTANCE;
    }

    private enum Internals {
        INSTANCE;
        private <T> T access(T value) {
            return value;
        }
    }

    public Class<?> getScopeType(Internals internals) {
        return scopeType;
    }

    public Extractor<T> getExtractor(Internals internals) {
        return internals.access(extractor);
    }

    public List<PropertyMatcher<T, ?>> getPropertyMatchers(Internals internals) {
        return internals.access(propertyMatchers);
    }

    public Capture<T> getCapture(Internals internals) {
        return internals.access(capture);
    }
}
