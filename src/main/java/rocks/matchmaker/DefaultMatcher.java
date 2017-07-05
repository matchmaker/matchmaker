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
            if (match.isEmpty()) {
                return (Match<T>) match;
            } else {
                return doCompute(pattern, match.value(), match.captures());
            }
        } else {
            return doCompute(pattern, object, captures);
        }
    }

    public <T> Match<T> doCompute(Pattern<T> pattern, Object object, Captures captures) {
        if (pattern instanceof EqualsPattern) {
            return Match.of((T) object, captures).filter(o -> ((EqualsPattern) pattern).expectedValue().equals(object));
        } else if (pattern instanceof CombinePattern) {
            CombinePattern<T> combinePattern = (CombinePattern<T>) pattern;
            return match(combinePattern.pattern(), object, captures);
        } else if (pattern instanceof WithPattern) {
            WithPattern<T> withPattern = (WithPattern<T>) pattern;
            Function<? super T, Option<?>> property = withPattern.getProperty();
            Option<?> propValue = property.apply((T) object);
            Match<?> propertyMatch = propValue
                    .map(value -> match(withPattern.getPattern(), value, captures))
                    .orElse(Match.empty());
            return propertyMatch.map(__ -> (T) object);
        } else if (pattern instanceof TypeOfPattern) {
            return Match.of((T) object, captures).filter(o -> ((TypeOfPattern) pattern).expectedClass().isInstance(object));
        } else if (pattern instanceof CapturePattern) {
            return Match.of((T) object, captures.addAll(Captures.ofNullable(((CapturePattern<T>) pattern).capture(), (T) object)));
        } else if (pattern instanceof FilterPattern) {
            return Match.of((T) object, captures).filter(((FilterPattern<T>) pattern).predicate());
        } else if (pattern instanceof ExtractPattern) {
            ExtractPattern<Object, T> extractPattern = (ExtractPattern<Object, T>) pattern;
            Extractor<Object, T> extractor = extractPattern.extractor();
            return extractor.apply(object, captures)
                    .map(v -> Match.of(v, captures))
                    .orElse(Match.empty());
        } else {
            throw new UnsupportedOperationException("Unsupported pattern type: " + pattern.getClass());
        }
    }
}
