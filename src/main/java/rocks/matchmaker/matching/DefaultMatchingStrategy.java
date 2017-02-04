package rocks.matchmaker.matching;

import rocks.matchmaker.Capture;
import rocks.matchmaker.Captures;
import rocks.matchmaker.Extractor;
import rocks.matchmaker.Match;
import rocks.matchmaker.Matcher;
import rocks.matchmaker.MatchingStrategy;
import rocks.matchmaker.Option;
import rocks.matchmaker.PropertyMatcher;

import java.util.Iterator;
import java.util.function.Function;

import static rocks.matchmaker.Matcher.internals;

public class DefaultMatchingStrategy implements MatchingStrategy {

    public static final MatchingStrategy INSTANCE = new DefaultMatchingStrategy();

    @Override
    public <T> Match<T> match(Matcher<T> matcher, Object object, Captures captures) {
        Match<T> selfMatch = matchSelf(matcher, object, captures);
        return matchProperties(selfMatch, matcher.getPropertyMatchers(internals()));
    }

    protected <T> Match<T> matchSelf(Matcher<T> matcher, Object object, Captures captures) {
        Extractor<T> extractor = matcher.getExtractor(internals());
        Capture<T> capture = matcher.getCapture(internals());
        Option<T> extractionResult = extractor.apply(object, captures);
        return extractionResult
                .map(value -> createMatch(value, captures, capture))
                .orElse(Match.empty());
    }

    protected <T> Match<T> createMatch(T value, Captures captures, Capture<T> capture) {
        return Match.of(value, captures.addAll(Captures.ofNullable(capture, value)));
    }

    //TODO express it in an idiomatic way - this is similar to Haskell's mapM
    protected <T> Match<T> matchProperties(Match<T> selfMatch, Iterable<PropertyMatcher<T, ?>> propertyMatchers) {
        Iterator<PropertyMatcher<T, ?>> iterator = propertyMatchers.iterator();
        while (iterator.hasNext() && selfMatch.isPresent()) {
            PropertyMatcher<T, ?> propertyMatcher = iterator.next();
            T selfValue = selfMatch.value();
            Match<?> propertyMatch = matchProperty(propertyMatcher, selfValue, selfMatch.captures());
            selfMatch = replaceResult(propertyMatch, selfValue);
        }
        return selfMatch;
    }

    private <T> Match<T> replaceResult(Match<?> match, T resultReplacement) {
        return match.map(__ -> resultReplacement);
    }

    protected <T> Match<?> matchProperty(PropertyMatcher<T, ?> propertyMatcher, T selfValue, Captures captures) {
        return evaluateProperty(selfValue, propertyMatcher.getProperty())
                .map(propertyValue -> propertyMatcher.getMatcher().match(propertyValue, captures))
                .orElse(Match.empty());
    }

    protected <T> Option<?> evaluateProperty(T selfValue, Function<T, Option<?>> property) {
        return property.apply(selfValue);
    }
}
