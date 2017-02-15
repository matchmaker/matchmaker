package rocks.matchmaker;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public class Matcher<T> {

    public static Matcher<Object> $() {
        return $(Object.class);
    }

    @SuppressWarnings("unchecked cast")
    public static <T> Matcher<T> isNull() {
        return (Matcher<T>) $(Object.class, x -> x == null);
    }

    public static <T> Matcher<T> equalTo(T expectedValue) {
        Class<T> expectedClass = (Class<T>) expectedValue.getClass();
        return $(expectedClass, (x) -> x.equals(expectedValue));
    }

    public static <T> Matcher<T> $(Class<T> expectedClass) {
        return $(expectedClass, (x) -> true);
    }

    public static <T> Matcher<T> $(Class<T> targetClass, Predicate<T> predicate) {
        return $(Extractor.assumingType(targetClass, (x) -> Option.of(x).filter(predicate)));
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
    public static <T> Matcher<T> $(Extractor.Scoped<?, T> extractor) {
        Capture<T> capture = null;
        return new Matcher<>(extractor.getScopeType(), toMatchFunction(extractor, capture), capture);
    }

    //This expresses the fact that Matcher is covariant on T.
    //This is saying "Matcher<? extends T> is a Matcher<T>".
    @SuppressWarnings("unchecked cast")
    public static <T> Matcher<T> upcast(Matcher<? extends T> matcher) {
        return (Matcher<T>) matcher;
    }

    //scopeType unused for now, but will help in debugging and structural matching later
    private final Class<?> scopeType;
    private final BiFunction<Object, Captures, Match<T>> matchFunction;
    private final Capture<T> capture;

    //TODO think how to not have this package-private? Make Matcher an interface?
    Matcher(Class<?> scopeType, BiFunction<Object, Captures, Match<T>> matchFunction, Capture<T> capture) {
        this.scopeType = scopeType;
        this.matchFunction = matchFunction;
        this.capture = capture;
    }

    private static <T> BiFunction<Object, Captures, Match<T>> toMatchFunction(Extractor<T> extractor, Capture<T> capture) {
        return (object, captures) -> extractor.apply(object, captures)
                .map(value -> createMatch(capture, value, captures))
                .orElse(Match.empty());
    }

    protected static <T> Match<T> createMatch(Capture<T> capture, T matchedValue, Captures captures) {
        return Match.of(matchedValue, captures.addAll(Captures.ofNullable(capture, matchedValue)));
    }

    public Matcher<T> capturedAs(Capture<T> capture) {
        if (this.capture != null) {
            throw new IllegalStateException("This matcher already has a capture alias");
        }
        BiFunction<Object, Captures, Match<T>> newMatchFunction =
                matchFunction.andThen(match -> match.flatMap(value -> createMatch(capture, value, match.captures())));
        return new Matcher<>(scopeType, newMatchFunction, capture);
    }

    public Matcher<T> matching(Predicate<T> predicate) {
        return new Matcher<>(scopeType, matchFunction.andThen(match -> match.filter(predicate)), capture);
    }

    public Matcher<T> matching(Extractor.Scoped<?, T> extractor) {
        return matching($(extractor));
    }

    public <S> Matcher<T> matching(Matcher<S> matcher) {
        PropertyMatcher<T, S> selfPropertyMatcher = new PropertyMatcher<>(Option::of, matcher);
        return with(selfPropertyMatcher);
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

    protected <R> Matcher<R> flatMap(BiFunction<T, Captures, Match<R>> mapper) {
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
