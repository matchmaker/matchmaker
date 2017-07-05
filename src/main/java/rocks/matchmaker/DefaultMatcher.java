package rocks.matchmaker;

import rocks.matchmaker.pattern.CapturePattern;
import rocks.matchmaker.pattern.CombinePattern;
import rocks.matchmaker.pattern.EqualsPattern;
import rocks.matchmaker.pattern.ExtractPattern;
import rocks.matchmaker.pattern.FilterPattern;
import rocks.matchmaker.pattern.TypeOfPattern;
import rocks.matchmaker.pattern.WithPattern;

import java.util.function.Function;

public class DefaultMatcher implements Matcher {

    public static final Matcher DEFAULT_MATCHER = new DefaultMatcher();

    @Override
    public <T> Match<T> match(Pattern<T> pattern, Object object, Captures captures) {
        if (pattern.previous() != null) {
            Match<?> match = match(pattern.previous(), object, captures);
            return match.flatMap((value) -> pattern.accept(this, value, match.captures()));
        } else {
            return pattern.accept(this, object, captures);
        }
    }

    @Override
    public <T> Match<T> visit(EqualsPattern<T> equalsPattern, Object object, Captures captures) {
        T expectedValue = equalsPattern.expectedValue();
        Class<T> expectedValueClass = (Class<T>) expectedValue.getClass();
        if (expectedValue.equals(object)) {
            return Match.of(expectedValueClass.cast(object), captures);
        } else {
            return Match.empty();
        }
    }

    @Override
    public <T> Match<T> visit(CombinePattern<T> combinePattern, Object object, Captures captures) {
        return match(combinePattern.pattern(), object, captures);
    }

    @Override
    public <T> Match<T> visit(WithPattern<T> withPattern, Object object, Captures captures) {
        Function<? super T, Option<?>> property = withPattern.getProperty();
        Option<?> propValue = property.apply((T) object);
        Match<?> propertyMatch = propValue
                .map(value -> match(withPattern.getPattern(), value, captures))
                .orElse(Match.empty());
        return propertyMatch.map(__ -> (T) object);
    }

    @Override
    public <T> Match<T> visit(TypeOfPattern<T> typeOfPattern, Object object, Captures captures) {
        Class<T> expectedClass = typeOfPattern.expectedClass();
        if (expectedClass.isInstance(object)) {
            return Match.of(expectedClass.cast(object), captures);
        } else {
            return Match.empty();
        }
    }

    @Override
    public <T> Match<T> visit(CapturePattern<T> capturePattern, Object object, Captures captures) {
        return Match.of((T) object, captures.addAll(Captures.ofNullable(capturePattern.capture(), (T) object)));

    }

    @Override
    public <T> Match<T> visit(FilterPattern<T> filterPattern, Object object, Captures captures) {
        return Match.of((T) object, captures).filter(filterPattern.predicate());
    }

    @Override
    public <T, R> Match<R> visit(ExtractPattern<T, R> extractPattern, Object object, Captures captures) {
        Extractor<T, R> extractor = extractPattern.extractor();
        return extractor.apply((T) object, captures)
                .map(v -> Match.of(v, captures))
                .orElse(Match.empty());
    }

}
