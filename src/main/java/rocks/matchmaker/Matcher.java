package rocks.matchmaker;

import rocks.matchmaker.util.Util;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class Matcher<T> {

    public static Matcher<Object> any() {
        return typeOf(Object.class);
    }

    public static <T> Matcher<T> equalTo(T expectedValue) {
        Util.checkArgument(expectedValue != null, "expectedValue can't be null. Use `Matcher.isNull()` instead");
        Class<T> expectedClass = (Class<T>) expectedValue.getClass();
        return typeOf(expectedClass).matching(x -> x.equals(expectedValue));
    }

    public static <T> Matcher<T> typeOf(Class<T> expectedClass) {
        BiFunction<Object, Captures, Match<T>> matchFunction = (x, captures) -> Match.of(x, captures)
                .filter(expectedClass::isInstance)
                .map(expectedClass::cast);
        return new Matcher<>(expectedClass, matchFunction, null);
    }

    @SuppressWarnings("unchecked cast")
    public static <T> Matcher<T> isNull() {
        return (Matcher<T>) nullable(Object.class).matching(Objects::isNull);
    }

    public static <T> Matcher<T> nullable(Class<T> expectedClass) {
        BiFunction<Object, Captures, Match<T>> matchFunction = (x, captures) -> Match.of(x, captures)
                .filter(value -> value == null || expectedClass.isInstance(value))
                .map(expectedClass::cast);
        return new Matcher<>(expectedClass, matchFunction, null);
    }

    //This expresses the fact that Matcher is covariant on T.
    //This is saying "Matcher<? extends T> is a Matcher<T>".
    @SuppressWarnings("unchecked cast")
    public static <T> Matcher<T> upcast(Matcher<? extends T> matcher) {
        return (Matcher<T>) matcher;
    }

    //scopeType unused for now, but will help in debugging and structural equalTo later
    private final Class<?> scopeType;
    private final BiFunction<Object, Captures, Match<T>> matchFunction;
    private final Capture<T> capture;

    //TODO think how to not have this package-private? Make Matcher an interface?
    Matcher(Class<?> scopeType, BiFunction<Object, Captures, Match<T>> matchFunction, Capture<T> capture) {
        this.scopeType = scopeType;
        this.matchFunction = matchFunction;
        this.capture = capture;
    }

    protected static <T> Match<T> createMatch(Capture<T> capture, T matchedValue, Captures captures) {
        return Match.of(matchedValue, captures.addAll(Captures.ofNullable(capture, matchedValue)));
    }

    public Matcher<T> capturedAs(Capture<T> capture) {
        if (this.capture != null) {
            throw new IllegalStateException("This matcher already has a capture alias");
        }
        return flatMap((value, captures) -> Match.of(value, captures)
                .flatMap(v -> createMatch(capture, v, captures)));
    }

    public Matcher<T> matching(Predicate<? super T> predicate) {
        return flatMap((value, captures) -> Match.of(value, captures)
                .filter(predicate));
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
     * @param <R>       type of the extracted value
     * @return
     */
    public <R> Matcher<R> matching(Extractor<T, R> extractor) {
        return flatMap((value, captures) -> extractor.apply(value, captures)
                .map(v -> Match.of(v, captures))
                .orElse(Match.empty()));
    }

    public <R> Matcher<R> matching(Matcher<R> matcher) {
        return flatMap(matcher.matchFunction);
    }

    public <R> Matcher<T> with(PropertyMatcher<? super T, R> matcher) {
        PropertyMatcher<T, R> castMatcher = PropertyMatcher.upcast(matcher);
        return this.flatMap((selfMatchValue, captures) -> {
            Option<?> propertyOption = castMatcher.getProperty().apply(selfMatchValue);
            Match<R> propertyMatch = propertyOption
                    .map(value -> castMatcher.getMatcher().match(value, captures))
                    .orElse(Match.empty());
            return propertyMatch.map(__ -> selfMatchValue);
        });
    }

    protected <R> Matcher<R> flatMap(BiFunction<? super T, Captures, Match<R>> mapper) {
        BiFunction<Object, Captures, Match<R>> newMatchFunction = (object, captures) -> {
            Match<T> originalMatch = matchFunction.apply(object, captures);
            return originalMatch.flatMap(value -> mapper.apply(value, originalMatch.captures()));
        };
        return new Matcher<>(scopeType, newMatchFunction, null);
    }

    //Usage of this method within the library's code almost always means an error because of lost captures.
    public Match<T> match(Object object) {
        return match(object, Captures.empty());
    }

    public Match<T> match(Object object, Captures captures) {
        return matchFunction.apply(object, captures);
    }

    Class<?> getScopeType() {
        return scopeType;
    }
}
