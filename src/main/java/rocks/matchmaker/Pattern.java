package rocks.matchmaker;

import rocks.matchmaker.pattern.CapturePattern;
import rocks.matchmaker.pattern.CombinePattern;
import rocks.matchmaker.pattern.EqualsPattern;
import rocks.matchmaker.pattern.ExtractPattern;
import rocks.matchmaker.pattern.FilterPattern;
import rocks.matchmaker.pattern.TypeOfPattern;
import rocks.matchmaker.pattern.WithPattern;
import rocks.matchmaker.util.Util;

import java.util.function.Predicate;

abstract public class Pattern<T> {

    private final Pattern<?> previous;

    public static Pattern<Object> any() {
        return typeOf(Object.class);
    }

    public static <T> Pattern<T> equalTo(T expectedValue) {
        Util.checkArgument(expectedValue != null, "expectedValue can't be null. Use `Pattern.isNull()` instead");
        return new EqualsPattern<>(expectedValue);
    }

    public static <T> Pattern<T> typeOf(Class<T> expectedClass) {
        return new TypeOfPattern<>(expectedClass);
    }

    //This expresses the fact that Pattern is covariant on T.
    //This is saying "Pattern<? extends T> is a Pattern<T>".
    @SuppressWarnings("unchecked cast")
    public static <T> Pattern<T> upcast(Pattern<? extends T> pattern) {
        return (Pattern<T>) pattern;
    }

    //FIXME this is temporary and only for the migration
    protected Pattern() {
        this(null);
    }

    protected Pattern(Pattern<?> previous) {
        this.previous = previous;
    }

    public Pattern<T> capturedAs(Capture<T> capture) {
        return new CapturePattern<>(capture, this);
    }

    public Pattern<T> matching(Predicate<? super T> predicate) {
        return new FilterPattern<>(predicate, this);
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
    public <R> Pattern<R> matching(Extractor<T, R> extractor) {
        return new ExtractPattern<>(extractor, this);
    }

    public <R> Pattern<R> matching(Pattern<R> pattern) {
        return new CombinePattern<>(this, pattern);
    }

    public Pattern<T> with(PropertyPattern<? super T, ?> pattern) {
        return new WithPattern<>(pattern, this);
    }

    public Pattern<?> previous() {
        return previous;
    }

    abstract public Match<T> accept(Matcher matcher, Object object, Captures captures);

    abstract public void accept(PatternVisitor patternVisitor);
}
