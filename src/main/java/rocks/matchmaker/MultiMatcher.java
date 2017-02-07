package rocks.matchmaker;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class MultiMatcher<T, R> extends Matcher<R> {

    public MultiMatcher(Map<Matcher<? extends T>, Function<T, R>> cases) {
        //TODO make this.scopeType = leastCommonSuperType(cases.keySet()*.scopeType)
        super(Object.class, createMatchFunction(new LinkedHashMap<>(cases)), null);
    }

    private static <T, R> BiFunction<Object, Captures, Match<R>> createMatchFunction(Map<Matcher<? extends T>, Function<T, R>> cases) {
        return (object, captures) -> {
            Stream<Match<R>> successfulCases = successfulCases(cases, object);
            Match<R> caseResult = successfulCases.findFirst().orElse(Match.empty());
            return caseResult;
        };
    }

    static <T, R> Stream<Match<R>> successfulCases(Map<Matcher<? extends T>, Function<T, R>> cases, Object object) {
        Stream<Match<R>> caseResults = cases.entrySet().stream().map(patternCase -> {
            Matcher<T> castMatcher = covariantUpcast(patternCase.getKey());
            Match<T> match = castMatcher.match(object);
            Function<T, R> matchToCaseResultMapper = patternCase.getValue();
            return match.map(matchToCaseResultMapper);
        });
        return caseResults.filter(Match::isPresent);
    }
}
